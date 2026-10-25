package Proconnect.backProconnect.repository;

import Proconnect.backProconnect.Entity.Posts;
import Proconnect.backProconnect.repository.projection.FeedPostRow;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Posts. El feed y el perfil leen cada página en UNA query (post + autor + contadores
 * denormalizados) ordenada por {@code (created_at DESC, id DESC)} sobre los índices {@code
 * idx_posts_feed (created_at, id)} e {@code idx_posts_author_feed (user_id, created_at, id)}. El
 * predicado del cursor usa {@code OR} en vez de comparación de tuplas para que MySQL haga un range
 * scan del índice (verificado con EXPLAIN, design D4). Se pide {@code limit + 1} filas para saber
 * si hay página siguiente sin COUNT.
 */
@Repository
public interface PostsRepository extends JpaRepository<Posts, Long> {

    String FEED_SELECT =
            "SELECT new Proconnect.backProconnect.repository.projection.FeedPostRow("
                    + "p.id, p.title, p.content, p.imageUrl, p.createdAt, p.likeCount,"
                    + " p.commentCount, u.id, u.name, u.userName, u.email, u.role, u.avatarUrl,"
                    + " u.coverUrl) FROM Posts p JOIN p.author u ";

    String ORDER = " ORDER BY p.createdAt DESC, p.id DESC";

    String AFTER = "(p.createdAt < :ts OR (p.createdAt = :ts AND p.id < :id))";

    @Query(FEED_SELECT + ORDER)
    List<FeedPostRow> feedFirstPage(Limit limit);

    @Query(FEED_SELECT + "WHERE " + AFTER + ORDER)
    List<FeedPostRow> feedAfter(@Param("ts") Instant ts, @Param("id") Long id, Limit limit);

    @Query(FEED_SELECT + "WHERE p.userId = :authorId" + ORDER)
    List<FeedPostRow> byAuthorFirstPage(@Param("authorId") Long authorId, Limit limit);

    @Query(FEED_SELECT + "WHERE p.userId = :authorId AND " + AFTER + ORDER)
    List<FeedPostRow> byAuthorAfter(
            @Param("authorId") Long authorId,
            @Param("ts") Instant ts,
            @Param("id") Long id,
            Limit limit);

    /**
     * Suma {@code delta} al contador de likes en un UPDATE atómico (bloquea la fila del post hasta
     * el commit). Devuelve 0 si el post no existe.
     */
    @Modifying(flushAutomatically = true)
    @Query("UPDATE Posts p SET p.likeCount = p.likeCount + :delta WHERE p.id = :id")
    int addLikes(@Param("id") Long id, @Param("delta") int delta);

    /** Igual que {@link #addLikes} para el contador de comentarios. */
    @Modifying(flushAutomatically = true)
    @Query("UPDATE Posts p SET p.commentCount = p.commentCount + :delta WHERE p.id = :id")
    int addComments(@Param("id") Long id, @Param("delta") int delta);
}
