-- EXPLAIN de las queries nativas actuales (tarea 1.5) sobre el dataset de referencia.
-- Variables: @viewer (feed), @profile_user (perfil), @post (comentarios), @com (un comentario).
-- Se ejecuta con perf/bench/explain.sh, que fija las variables a los targets de targets.mjs.

SELECT '==== 1. PostsRepository.GetAllPosts(userId) — EXPLAIN (tabular)' AS section;
EXPLAIN
SELECT pos.id, pos.title, pos.content, pos.date, pos.image_url,
       us.id as userId, us.email, us.name, us.role, us.user_name,
       prof.id as profileId, prof.photo_url, prof.wall_photo_url,
       (SELECT COUNT(*) FROM proconnect.likes WHERE post_id = pos.id) as total_likes,
       (SELECT COUNT(*) FROM proconnect.comments WHERE post_id = pos.id) as total_comments,
       (SELECT COUNT(*) FROM proconnect.likes WHERE post_id = pos.id AND user_id = @viewer) as user_liked
FROM proconnect.posts as pos
JOIN proconnect.user as us ON pos.user_id = us.id
LEFT JOIN proconnect.profile_photos as prof ON us.id = prof.user_id;

SELECT '==== 1b. PostsRepository.GetAllPosts(userId) — EXPLAIN ANALYZE (ejecuta la query)' AS section;
EXPLAIN ANALYZE
SELECT pos.id, pos.title, pos.content, pos.date, pos.image_url,
       us.id as userId, us.email, us.name, us.role, us.user_name,
       prof.id as profileId, prof.photo_url, prof.wall_photo_url,
       (SELECT COUNT(*) FROM proconnect.likes WHERE post_id = pos.id) as total_likes,
       (SELECT COUNT(*) FROM proconnect.comments WHERE post_id = pos.id) as total_comments,
       (SELECT COUNT(*) FROM proconnect.likes WHERE post_id = pos.id AND user_id = @viewer) as user_liked
FROM proconnect.posts as pos
JOIN proconnect.user as us ON pos.user_id = us.id
LEFT JOIN proconnect.profile_photos as prof ON us.id = prof.user_id;

SELECT '==== 2. PostsRepository.GetAllPosts() — EXPLAIN (tabular)' AS section;
EXPLAIN
SELECT pos.id, pos.title, pos.content, pos.date, pos.image_url,
       us.id as userId, us.email, us.name, us.role, us.user_name,
       prof.id as profileId, prof.photo_url, prof.wall_photo_url,
       (SELECT COUNT(*) FROM proconnect.likes WHERE post_id = pos.id) as total_likes,
       (SELECT COUNT(*) FROM proconnect.comments WHERE post_id = pos.id) as total_comments
FROM proconnect.posts as pos
JOIN proconnect.user as us ON pos.user_id = us.id
LEFT JOIN proconnect.profile_photos as prof ON us.id = prof.user_id;

SELECT '==== 3. UserRepository.findUsersWithProfilesAndPosts(userId) — EXPLAIN (tabular)' AS section;
EXPLAIN
SELECT us.id, us.email, us.name, us.user_name, us.role, pr.photo_url, pr.wall_photo_url,
       pos.id as post_id, pos.title, pos.content as post_content, pos.date as post_date, pos.image_url as post_image,
       com.content as contenidoComentario, com.date as fechaComentario, com.id as id_com, com.user_id,
       (SELECT COUNT(*) FROM proconnect.likes WHERE post_id = pos.id) as total_likes,
       (SELECT COUNT(*) FROM proconnect.comments WHERE post_id = pos.id) as total_comments,
       (SELECT COUNT(*) FROM proconnect.likes WHERE post_id = pos.id AND user_id = @profile_user) as user_liked
FROM proconnect.user as us
LEFT JOIN proconnect.profile_photos as pr ON us.id = pr.user_id
LEFT JOIN proconnect.posts as pos ON us.id = pos.user_id
LEFT JOIN proconnect.comments as com ON pos.id = com.post_id
WHERE us.id = @profile_user
ORDER BY pos.date DESC;

SELECT '==== 3b. UserRepository.findUsersWithProfilesAndPosts(userId) — EXPLAIN ANALYZE' AS section;
EXPLAIN ANALYZE
SELECT us.id, us.email, us.name, us.user_name, us.role, pr.photo_url, pr.wall_photo_url,
       pos.id as post_id, pos.title, pos.content as post_content, pos.date as post_date, pos.image_url as post_image,
       com.content as contenidoComentario, com.date as fechaComentario, com.id as id_com, com.user_id,
       (SELECT COUNT(*) FROM proconnect.likes WHERE post_id = pos.id) as total_likes,
       (SELECT COUNT(*) FROM proconnect.comments WHERE post_id = pos.id) as total_comments,
       (SELECT COUNT(*) FROM proconnect.likes WHERE post_id = pos.id AND user_id = @profile_user) as user_liked
FROM proconnect.user as us
LEFT JOIN proconnect.profile_photos as pr ON us.id = pr.user_id
LEFT JOIN proconnect.posts as pos ON us.id = pos.user_id
LEFT JOIN proconnect.comments as com ON pos.id = com.post_id
WHERE us.id = @profile_user
ORDER BY pos.date DESC;

SELECT '==== 4. CommentsRepository.findByPostId (derivada, N+1 del feed: 1 por post) — EXPLAIN ANALYZE' AS section;
EXPLAIN ANALYZE
SELECT c1_0.id, c1_0.content, c1_0.date, c1_0.post_id, c1_0.user_id FROM proconnect.comments c1_0 WHERE c1_0.post_id = @post;

SELECT '==== 5. CommentsRepository.findUserWithComment (N+1 del feed: 1 por comentario) — EXPLAIN ANALYZE' AS section;
EXPLAIN ANALYZE
SELECT us.id, us.email, us.name, us.role, us.user_name, prof.id, prof.photo_url, prof.wall_photo_url
FROM proconnect.comments as com
JOIN proconnect.user as us ON com.user_id = us.id
LEFT JOIN proconnect.profile_photos as prof ON us.id = prof.user_id
WHERE com.id = @com;

SELECT '==== 6. ProfilePhotosRepository.findByUserId (N+1 del perfil: 1 por comentario) — EXPLAIN ANALYZE' AS section;
EXPLAIN ANALYZE
SELECT pp1_0.id, pp1_0.photo_url, pp1_0.user_id, pp1_0.wall_photo_url FROM proconnect.profile_photos pp1_0 WHERE pp1_0.user_id = @profile_user;

SELECT '==== 7. LikeRepository.existsByUserIdAndPostId (like create) — EXPLAIN ANALYZE' AS section;
EXPLAIN ANALYZE
SELECT l1_0.id FROM proconnect.likes l1_0 WHERE l1_0.user_id = @viewer AND l1_0.post_id = @post LIMIT 1;

SELECT '==== Índices existentes' AS section;
SELECT TABLE_NAME, INDEX_NAME, GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX) cols, NON_UNIQUE
FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = 'proconnect'
GROUP BY TABLE_NAME, INDEX_NAME, NON_UNIQUE ORDER BY TABLE_NAME, INDEX_NAME;
