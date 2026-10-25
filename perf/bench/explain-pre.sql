-- EXPLAIN de las queries tras cleanup-codebase (4163ca1), antes de la fase 2 de
-- restructure-models-performance. Mismo SQL que PostsRepository.findFeed y
-- CommentsRepository.findWithAuthorByPostId. Se ejecuta con
--   perf/bench/explain.sh <label> explain-pre.sql
-- Variables: @viewer, @profile_user, @post (las fija explain.sh desde targets.mjs).

SELECT '==== 1. PostsRepository.findFeed(viewer, NULL) — feed completo, EXPLAIN ANALYZE' AS section;
EXPLAIN ANALYZE
SELECT
    pos.id AS id, pos.title AS title, pos.content AS content, pos.date AS date, pos.image_url AS imageUrl,
    us.id AS userId, us.email AS userEmail, us.name AS userName, us.role AS userRole,
    us.user_name AS userUserName,
    prof.photo_url AS photoUrl, prof.wall_photo_url AS wallPhotoUrl,
    (SELECT COUNT(*) FROM proconnect.likes WHERE post_id = pos.id) AS totalLikes,
    (SELECT COUNT(*) FROM proconnect.comments WHERE post_id = pos.id) AS totalComments,
    (SELECT COUNT(*) FROM proconnect.likes WHERE post_id = pos.id AND user_id = @viewer) AS userLiked
FROM proconnect.posts AS pos
JOIN proconnect.user AS us ON pos.user_id = us.id
LEFT JOIN proconnect.profile_photos AS prof ON prof.id =
    (SELECT MAX(p2.id) FROM proconnect.profile_photos p2 WHERE p2.user_id = us.id)
WHERE (NULL IS NULL OR pos.user_id = NULL)
ORDER BY pos.date DESC, pos.id DESC;

SELECT '==== 2. PostsRepository.findFeed(viewer, author) — posts del perfil, EXPLAIN ANALYZE' AS section;
EXPLAIN ANALYZE
SELECT
    pos.id, pos.title, pos.content, pos.date, pos.image_url,
    us.id, us.email, us.name, us.role, us.user_name,
    prof.photo_url, prof.wall_photo_url,
    (SELECT COUNT(*) FROM proconnect.likes WHERE post_id = pos.id) AS totalLikes,
    (SELECT COUNT(*) FROM proconnect.comments WHERE post_id = pos.id) AS totalComments,
    (SELECT COUNT(*) FROM proconnect.likes WHERE post_id = pos.id AND user_id = @viewer) AS userLiked
FROM proconnect.posts AS pos
JOIN proconnect.user AS us ON pos.user_id = us.id
LEFT JOIN proconnect.profile_photos AS prof ON prof.id =
    (SELECT MAX(p2.id) FROM proconnect.profile_photos p2 WHERE p2.user_id = us.id)
WHERE (@profile_user IS NULL OR pos.user_id = @profile_user)
ORDER BY pos.date DESC, pos.id DESC;

SELECT '==== 3. CommentsRepository.findWithAuthorByPostId (1 por post del feed) — EXPLAIN ANALYZE' AS section;
EXPLAIN ANALYZE
SELECT com.id, com.post_id, com.user_id, com.content, com.date,
       us.email, us.name, us.role, us.user_name, prof.photo_url, prof.wall_photo_url
FROM proconnect.comments AS com
JOIN proconnect.user AS us ON com.user_id = us.id
LEFT JOIN proconnect.profile_photos AS prof ON prof.id =
    (SELECT MAX(p2.id) FROM proconnect.profile_photos p2 WHERE p2.user_id = us.id)
WHERE com.post_id = @post
ORDER BY com.date ASC, com.id ASC;

SELECT '==== 4. LikeRepository.existsByUserIdAndPostId (like) — EXPLAIN ANALYZE' AS section;
EXPLAIN ANALYZE
SELECT l1_0.id FROM proconnect.likes l1_0 WHERE l1_0.user_id = @viewer AND l1_0.post_id = @post LIMIT 1;

SELECT '==== 5. ProfilePhotosRepository.findFirstByUserIdOrderByIdDesc (perfil) — EXPLAIN ANALYZE' AS section;
EXPLAIN ANALYZE
SELECT pp1_0.id, pp1_0.photo_url, pp1_0.user_id, pp1_0.wall_photo_url
FROM proconnect.profile_photos pp1_0 WHERE pp1_0.user_id = @profile_user ORDER BY pp1_0.id DESC LIMIT 1;

SELECT '==== Índices existentes' AS section;
SELECT TABLE_NAME, INDEX_NAME, GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX) cols, NON_UNIQUE
FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = 'proconnect'
GROUP BY TABLE_NAME, INDEX_NAME, NON_UNIQUE ORDER BY TABLE_NAME, INDEX_NAME;
