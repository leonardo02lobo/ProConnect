-- V2: integridad referencial, unicidad e índices para los patrones de acceso reales.
--
-- ANTES DE APLICAR EN UNA BASE CON DATOS: backup obligatorio (docs/db/migrations.md):
--   mysqldump --single-transaction --routines proconnect > proconnect-pre-V2.sql
-- La migración es destructiva SOLO para filas inválidas: borra huérfanos (filas que apuntan
-- a un usuario/post/empleo/empresa inexistente) y duplicados (se conserva el más antiguo,
-- salvo profile_photos, donde se conserva la fila más reciente, que es la que leía la app).
-- Cada limpieza deja antes su recuento en `migration_audit` (auditoría persistente) y en el
-- log de Flyway (SELECT).

CREATE TABLE IF NOT EXISTS `migration_audit` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `version` varchar(16) NOT NULL,
  `check_name` varchar(64) NOT NULL,
  `affected_rows` bigint NOT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ---------------------------------------------------------------------------
-- 1. Auditoría previa (se registra todo antes de borrar nada)
-- ---------------------------------------------------------------------------
INSERT INTO migration_audit (version, check_name, affected_rows)
SELECT 'V2', 'orphan_profile_photos', COUNT(*) FROM profile_photos WHERE user_id NOT IN (SELECT id FROM `user`)
UNION ALL SELECT 'V2', 'orphan_posts', COUNT(*) FROM posts WHERE user_id NOT IN (SELECT id FROM `user`)
UNION ALL SELECT 'V2', 'orphan_comments', COUNT(*) FROM comments
    WHERE post_id NOT IN (SELECT p.id FROM posts p JOIN `user` u ON u.id = p.user_id)
       OR user_id NOT IN (SELECT id FROM `user`)
UNION ALL SELECT 'V2', 'orphan_likes', COUNT(*) FROM likes
    WHERE post_id NOT IN (SELECT p.id FROM posts p JOIN `user` u ON u.id = p.user_id)
       OR user_id NOT IN (SELECT id FROM `user`)
UNION ALL SELECT 'V2', 'orphan_friends', COUNT(*) FROM friends
    WHERE user_id_follow NOT IN (SELECT id FROM `user`) OR user_id_be_followed NOT IN (SELECT id FROM `user`)
UNION ALL SELECT 'V2', 'orphan_messages', COUNT(*) FROM messages
    WHERE sender_id NOT IN (SELECT id FROM `user`) OR receiver_id NOT IN (SELECT id FROM `user`)
UNION ALL SELECT 'V2', 'orphan_company', COUNT(*) FROM company WHERE user_id NOT IN (SELECT id FROM `user`)
UNION ALL SELECT 'V2', 'orphan_jobs', COUNT(*) FROM jobs
    WHERE company_id NOT IN (SELECT c.id FROM company c JOIN `user` u ON u.id = c.user_id)
UNION ALL SELECT 'V2', 'orphan_postulations', COUNT(*) FROM postulations
    WHERE user_id NOT IN (SELECT id FROM `user`)
       OR job_id NOT IN (SELECT j.id FROM jobs j JOIN company c ON c.id = j.company_id JOIN `user` u ON u.id = c.user_id)
UNION ALL SELECT 'V2', 'orphan_work_stations', COUNT(*) FROM work_stations WHERE user_id NOT IN (SELECT id FROM `user`)
UNION ALL SELECT 'V2', 'duplicate_likes', COUNT(*) - COUNT(DISTINCT user_id, post_id) FROM likes
UNION ALL SELECT 'V2', 'duplicate_friends', COUNT(*) - COUNT(DISTINCT user_id_follow, user_id_be_followed) FROM friends
UNION ALL SELECT 'V2', 'duplicate_postulations', COUNT(*) - COUNT(DISTINCT user_id, job_id) FROM postulations
UNION ALL SELECT 'V2', 'duplicate_profile_photos', COUNT(*) - COUNT(DISTINCT user_id) FROM profile_photos;

SELECT check_name, affected_rows FROM migration_audit WHERE version = 'V2' AND affected_rows > 0;

-- ---------------------------------------------------------------------------
-- 2. Huérfanos (de hijos a padres)
-- ---------------------------------------------------------------------------
DELETE FROM profile_photos WHERE user_id NOT IN (SELECT id FROM `user`);
DELETE FROM posts WHERE user_id NOT IN (SELECT id FROM `user`);
DELETE FROM comments WHERE post_id NOT IN (SELECT id FROM posts) OR user_id NOT IN (SELECT id FROM `user`);
DELETE FROM likes WHERE post_id NOT IN (SELECT id FROM posts) OR user_id NOT IN (SELECT id FROM `user`);
DELETE FROM friends WHERE user_id_follow NOT IN (SELECT id FROM `user`) OR user_id_be_followed NOT IN (SELECT id FROM `user`);
DELETE FROM messages WHERE sender_id NOT IN (SELECT id FROM `user`) OR receiver_id NOT IN (SELECT id FROM `user`);
DELETE FROM company WHERE user_id NOT IN (SELECT id FROM `user`);
DELETE FROM jobs WHERE company_id NOT IN (SELECT id FROM company);
DELETE FROM postulations WHERE user_id NOT IN (SELECT id FROM `user`) OR job_id NOT IN (SELECT id FROM jobs);
DELETE FROM work_stations WHERE user_id NOT IN (SELECT id FROM `user`);

-- ---------------------------------------------------------------------------
-- 3. Duplicados (se conserva la fila más antigua = menor id; en fotos, la más reciente)
-- ---------------------------------------------------------------------------
DELETE FROM likes WHERE id NOT IN (
    SELECT keep_id FROM (SELECT MIN(id) AS keep_id FROM likes GROUP BY user_id, post_id) k);
DELETE FROM friends WHERE id NOT IN (
    SELECT keep_id FROM (SELECT MIN(id) AS keep_id FROM friends GROUP BY user_id_follow, user_id_be_followed) k);
DELETE FROM postulations WHERE id NOT IN (
    SELECT keep_id FROM (SELECT MIN(id) AS keep_id FROM postulations GROUP BY user_id, job_id) k);
DELETE FROM profile_photos WHERE id NOT IN (
    SELECT keep_id FROM (SELECT MAX(id) AS keep_id FROM profile_photos GROUP BY user_id) k);

-- ---------------------------------------------------------------------------
-- 4. user.name deja de ser UNIQUE (índice con nombre aleatorio creado por Hibernate en
--    bases existentes; en bases nuevas V1 ya no lo crea)
-- ---------------------------------------------------------------------------
SET @uk_name := (
    SELECT MIN(s.INDEX_NAME) FROM information_schema.STATISTICS s
     WHERE s.TABLE_SCHEMA = DATABASE() AND s.TABLE_NAME = 'user' AND s.NON_UNIQUE = 0
       AND s.INDEX_NAME <> 'PRIMARY' AND s.COLUMN_NAME = 'name' AND s.SEQ_IN_INDEX = 1
       AND (SELECT COUNT(*) FROM information_schema.STATISTICS s2
             WHERE s2.TABLE_SCHEMA = s.TABLE_SCHEMA AND s2.TABLE_NAME = s.TABLE_NAME
               AND s2.INDEX_NAME = s.INDEX_NAME) = 1);
SET @drop_uk_name := IF(@uk_name IS NULL, 'DO 0', CONCAT('ALTER TABLE `user` DROP INDEX `', @uk_name, '`'));
PREPARE drop_uk_name FROM @drop_uk_name;
EXECUTE drop_uk_name;
DEALLOCATE PREPARE drop_uk_name;

-- ---------------------------------------------------------------------------
-- 5. UNIQUE e índices (antes que las FKs, para que MySQL no cree índices implícitos)
-- ---------------------------------------------------------------------------
-- feed global (created_at DESC, id DESC) y perfil (posts de un autor)
ALTER TABLE posts
    ADD INDEX idx_posts_feed (`date`, id),
    ADD INDEX idx_posts_author_feed (user_id, `date`, id);
-- comentarios por post (preview y listado paginado); autor para la FK
ALTER TABLE comments
    ADD INDEX idx_comments_post_feed (post_id, `date`, id),
    ADD INDEX idx_comments_user (user_id);
-- un like por (usuario, post): sirve también para "¿el viewer dio like?" en lote
ALTER TABLE likes
    ADD UNIQUE KEY uk_likes_user_post (user_id, post_id),
    ADD INDEX idx_likes_post (post_id);
ALTER TABLE friends
    ADD UNIQUE KEY uk_friends_pair (user_id_follow, user_id_be_followed),
    ADD INDEX idx_friends_followed_status (user_id_be_followed, status);
ALTER TABLE messages
    ADD INDEX idx_messages_receiver_sent (receiver_id, `timestamp`),
    ADD INDEX idx_messages_sender_sent (sender_id, `timestamp`);
ALTER TABLE company ADD INDEX idx_company_user (user_id);
ALTER TABLE jobs ADD INDEX idx_jobs_company (company_id);
ALTER TABLE postulations
    ADD UNIQUE KEY uk_postulations_user_job (user_id, job_id),
    ADD INDEX idx_postulations_job_status (job_id, status);
ALTER TABLE work_stations ADD INDEX idx_work_stations_user (user_id);
ALTER TABLE profile_photos ADD UNIQUE KEY uk_profile_photos_user (user_id);

-- ---------------------------------------------------------------------------
-- 6. Claves foráneas. CASCADE: contenido que depende de su padre (likes, comentarios,
--    fotos, postulaciones, amistades, mensajes y posts de un usuario borrado; empleos de
--    una empresa borrada). RESTRICT: no se puede borrar un usuario que es dueño de una
--    empresa (información de negocio de terceros: empleos y postulaciones).
-- ---------------------------------------------------------------------------
ALTER TABLE posts
    ADD CONSTRAINT fk_posts_user FOREIGN KEY (user_id) REFERENCES `user` (id) ON DELETE CASCADE;
ALTER TABLE comments
    ADD CONSTRAINT fk_comments_post FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_comments_user FOREIGN KEY (user_id) REFERENCES `user` (id) ON DELETE CASCADE;
ALTER TABLE likes
    ADD CONSTRAINT fk_likes_post FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_likes_user FOREIGN KEY (user_id) REFERENCES `user` (id) ON DELETE CASCADE;
ALTER TABLE friends
    ADD CONSTRAINT fk_friends_follow FOREIGN KEY (user_id_follow) REFERENCES `user` (id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_friends_followed FOREIGN KEY (user_id_be_followed) REFERENCES `user` (id) ON DELETE CASCADE;
ALTER TABLE messages
    ADD CONSTRAINT fk_messages_sender FOREIGN KEY (sender_id) REFERENCES `user` (id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_messages_receiver FOREIGN KEY (receiver_id) REFERENCES `user` (id) ON DELETE CASCADE;
ALTER TABLE company
    ADD CONSTRAINT fk_company_user FOREIGN KEY (user_id) REFERENCES `user` (id) ON DELETE RESTRICT;
ALTER TABLE jobs
    ADD CONSTRAINT fk_jobs_company FOREIGN KEY (company_id) REFERENCES company (id) ON DELETE CASCADE;
ALTER TABLE postulations
    ADD CONSTRAINT fk_postulations_job FOREIGN KEY (job_id) REFERENCES jobs (id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_postulations_user FOREIGN KEY (user_id) REFERENCES `user` (id) ON DELETE CASCADE;
ALTER TABLE work_stations
    ADD CONSTRAINT fk_work_stations_user FOREIGN KEY (user_id) REFERENCES `user` (id) ON DELETE CASCADE;
ALTER TABLE profile_photos
    ADD CONSTRAINT fk_profile_photos_user FOREIGN KEY (user_id) REFERENCES `user` (id) ON DELETE CASCADE;
