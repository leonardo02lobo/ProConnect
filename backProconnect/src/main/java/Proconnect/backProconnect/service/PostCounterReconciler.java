package Proconnect.backProconnect.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reconciliación de {@code posts.like_count}/{@code comment_count} con los {@code COUNT(*)} reales
 * (design D5). Los contadores se mantienen en la misma transacción que el like/comentario, así que
 * no deberían divergir; esto es una red de seguridad que recalcula y deja en el log cuántos posts
 * estaban mal. Desactivada por defecto: {@code COUNTERS_RECONCILE_CRON} (p. ej. {@code 0 30 3 * *
 * *}) la activa. SQL de MySQL.
 */
@Component
public class PostCounterReconciler {

    private static final Logger log = LoggerFactory.getLogger(PostCounterReconciler.class);

    private static final String MISMATCH_FILTER =
            " FROM posts p"
                    + " LEFT JOIN (SELECT post_id, COUNT(*) AS n FROM likes GROUP BY post_id) l"
                    + " ON l.post_id = p.id"
                    + " LEFT JOIN (SELECT post_id, COUNT(*) AS n FROM comments GROUP BY post_id) c"
                    + " ON c.post_id = p.id"
                    + " WHERE p.like_count <> IFNULL(l.n, 0) OR p.comment_count <> IFNULL(c.n, 0)";

    private final JdbcTemplate jdbc;

    public PostCounterReconciler(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Corrige los posts con contadores desalineados y devuelve cuántos había. */
    @Scheduled(cron = "${proconnect.counters.reconcile-cron:-}")
    @Transactional
    public int reconcile() {
        Integer mismatched =
                jdbc.queryForObject("SELECT COUNT(*)" + MISMATCH_FILTER, Integer.class);
        int found = mismatched == null ? 0 : mismatched;
        if (found > 0) {
            jdbc.update(
                    "UPDATE posts p"
                            + " LEFT JOIN (SELECT post_id, COUNT(*) AS n FROM likes GROUP BY"
                            + " post_id) l ON l.post_id = p.id"
                            + " LEFT JOIN (SELECT post_id, COUNT(*) AS n FROM comments GROUP BY"
                            + " post_id) c ON c.post_id = p.id"
                            + " SET p.like_count = IFNULL(l.n, 0), p.comment_count = IFNULL(c.n, 0)"
                            + " WHERE p.like_count <> IFNULL(l.n, 0) OR p.comment_count <>"
                            + " IFNULL(c.n, 0)");
            log.warn("Contadores de {} posts desalineados: recalculados", found);
        } else {
            log.info("Contadores de posts consistentes");
        }
        return found;
    }
}
