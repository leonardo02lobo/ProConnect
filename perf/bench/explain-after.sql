-- EXPLAIN de las queries de la fase 2 (restructure-models-performance) sobre el dataset de referencia.
-- Mismo SQL que genera Hibernate para PostsRepository / LikeRepository / CommentsRepository.
--   perf/bench/explain.sh <label> explain-after.sql
-- Variables (explain.sh): @viewer, @profile_user, @post, @deep_ts/@deep_id (cursor de la página profunda).

SET @ids := (SELECT GROUP_CONCAT(id) FROM (SELECT id FROM posts ORDER BY created_at DESC, id DESC LIMIT 20) t);
SET @deep_ids := (SELECT GROUP_CONCAT(id) FROM (SELECT id FROM posts
    WHERE created_at < @deep_ts OR (created_at = @deep_ts AND id < @deep_id)
    ORDER BY created_at DESC, id DESC LIMIT 20) t);

SELECT '==== Q1 feed, 1.ª página (feedFirstPage) — EXPLAIN + EXPLAIN ANALYZE' AS section;
EXPLAIN
SELECT p.id, p.title, p.content, p.image_url, p.created_at, p.like_count, p.comment_count,
       u.id, u.name, u.user_name, u.email, u.role, u.avatar_url, u.cover_url
FROM posts p JOIN `user` u ON u.id = p.user_id
ORDER BY p.created_at DESC, p.id DESC LIMIT 21;
EXPLAIN ANALYZE
SELECT p.id, p.title, p.content, p.image_url, p.created_at, p.like_count, p.comment_count,
       u.id, u.name, u.user_name, u.email, u.role, u.avatar_url, u.cover_url
FROM posts p JOIN `user` u ON u.id = p.user_id
ORDER BY p.created_at DESC, p.id DESC LIMIT 21;

SELECT '==== Q1 feed, página profunda con cursor (feedAfter, predicado con OR) — EXPLAIN + ANALYZE' AS section;
EXPLAIN
SELECT p.id, p.title, p.content, p.image_url, p.created_at, p.like_count, p.comment_count,
       u.id, u.name, u.user_name, u.email, u.role, u.avatar_url, u.cover_url
FROM posts p JOIN `user` u ON u.id = p.user_id
WHERE p.created_at < @deep_ts OR (p.created_at = @deep_ts AND p.id < @deep_id)
ORDER BY p.created_at DESC, p.id DESC LIMIT 21;
EXPLAIN ANALYZE
SELECT p.id, p.title, p.content, p.image_url, p.created_at, p.like_count, p.comment_count,
       u.id, u.name, u.user_name, u.email, u.role, u.avatar_url, u.cover_url
FROM posts p JOIN `user` u ON u.id = p.user_id
WHERE p.created_at < @deep_ts OR (p.created_at = @deep_ts AND p.id < @deep_id)
ORDER BY p.created_at DESC, p.id DESC LIMIT 21;

SELECT '==== Q1 alternativa descartada: comparación de tuplas — EXPLAIN ANALYZE' AS section;
EXPLAIN ANALYZE
SELECT p.id FROM posts p JOIN `user` u ON u.id = p.user_id
WHERE (p.created_at, p.id) < (@deep_ts, @deep_id)
ORDER BY p.created_at DESC, p.id DESC LIMIT 21;

SELECT '==== Q2 likes del viewer en la página (likedAmong) — EXPLAIN ANALYZE' AS section;
SET @q := CONCAT('EXPLAIN ANALYZE SELECT l.post_id FROM likes l WHERE l.user_id = ', @viewer, ' AND l.post_id IN (', @ids, ')');
PREPARE s FROM @q; EXECUTE s; DEALLOCATE PREPARE s;

SELECT '==== Q3 preview de 3 comentarios por post con ROW_NUMBER (latestPerPost), página profunda — EXPLAIN ANALYZE' AS section;
SET @q := CONCAT('EXPLAIN ANALYZE SELECT t.* FROM (SELECT c.id, c.post_id, c.user_id, c.content, c.created_at, u.name, u.user_name, u.email, u.role, u.avatar_url, u.cover_url, ROW_NUMBER() OVER (PARTITION BY c.post_id ORDER BY c.created_at DESC, c.id DESC) AS rn FROM comments c JOIN user u ON u.id = c.user_id WHERE c.post_id IN (', @deep_ids, ')) t WHERE t.rn <= 3 ORDER BY t.post_id, t.created_at, t.id');
PREPARE s FROM @q; EXECUTE s; DEALLOCATE PREPARE s;

SELECT '==== Perfil: posts del autor (byAuthorFirstPage) — EXPLAIN ANALYZE' AS section;
EXPLAIN ANALYZE
SELECT p.id, p.title, p.created_at, p.like_count, p.comment_count, u.id, u.name
FROM posts p JOIN `user` u ON u.id = p.user_id
WHERE p.user_id = @profile_user
ORDER BY p.created_at DESC, p.id DESC LIMIT 21;

SELECT '==== Comentarios paginados de un post (pageFirst) — EXPLAIN ANALYZE' AS section;
EXPLAIN ANALYZE
SELECT c.id, c.post_id, c.user_id, c.content, c.created_at, u.name, u.user_name, u.email, u.role, u.avatar_url, u.cover_url
FROM comments c JOIN `user` u ON u.id = c.user_id
WHERE c.post_id = @post ORDER BY c.created_at, c.id LIMIT 21;

SELECT '==== Like: contador + insert (UPDATE posts por PK) — EXPLAIN' AS section;
EXPLAIN UPDATE posts SET like_count = like_count + 1 WHERE id = @post;

SELECT '==== Índices existentes' AS section;
SELECT TABLE_NAME, INDEX_NAME, GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX) cols, NON_UNIQUE
FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = 'proconnect'
GROUP BY TABLE_NAME, INDEX_NAME, NON_UNIQUE ORDER BY TABLE_NAME, INDEX_NAME;
