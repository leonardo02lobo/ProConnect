-- V4: contadores denormalizados (design D5). Sustituyen a las subconsultas COUNT(*) correlacionadas
-- del feed. Los mantiene la app en la misma transacción que crea/borra el like o el comentario
-- (LikeService, CommentsService); PostCounterReconciler puede recalcularlos (desactivado por defecto).
ALTER TABLE posts
    ADD COLUMN like_count int NOT NULL DEFAULT 0,
    ADD COLUMN comment_count int NOT NULL DEFAULT 0;

UPDATE posts p
LEFT JOIN (SELECT post_id, COUNT(*) AS n FROM likes GROUP BY post_id) l ON l.post_id = p.id
LEFT JOIN (SELECT post_id, COUNT(*) AS n FROM comments GROUP BY post_id) c ON c.post_id = p.id
SET p.like_count = IFNULL(l.n, 0), p.comment_count = IFNULL(c.n, 0);

-- Test de consistencia de la migración: debe quedar 0 (spec "Consistencia tras la migración").
INSERT INTO migration_audit (version, check_name, affected_rows)
SELECT 'V4', 'counter_mismatch_after_backfill', COUNT(*) FROM posts p
 WHERE p.like_count <> (SELECT COUNT(*) FROM likes l WHERE l.post_id = p.id)
    OR p.comment_count <> (SELECT COUNT(*) FROM comments c WHERE c.post_id = p.id);
