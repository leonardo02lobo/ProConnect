-- Rollback MANUAL de V2 (Flyway nunca ejecuta esta carpeta). Ver docs/db/migrations.md.
-- No recupera las filas borradas por V2 (huérfanos/duplicados): restaurar el backup para eso.
ALTER TABLE posts DROP FOREIGN KEY fk_posts_user;
ALTER TABLE comments DROP FOREIGN KEY fk_comments_post, DROP FOREIGN KEY fk_comments_user;
ALTER TABLE likes DROP FOREIGN KEY fk_likes_post, DROP FOREIGN KEY fk_likes_user;
ALTER TABLE friends DROP FOREIGN KEY fk_friends_follow, DROP FOREIGN KEY fk_friends_followed;
ALTER TABLE messages DROP FOREIGN KEY fk_messages_sender, DROP FOREIGN KEY fk_messages_receiver;
ALTER TABLE company DROP FOREIGN KEY fk_company_user;
ALTER TABLE jobs DROP FOREIGN KEY fk_jobs_company;
ALTER TABLE postulations DROP FOREIGN KEY fk_postulations_job, DROP FOREIGN KEY fk_postulations_user;
ALTER TABLE work_stations DROP FOREIGN KEY fk_work_stations_user;
ALTER TABLE profile_photos DROP FOREIGN KEY fk_profile_photos_user;

ALTER TABLE posts DROP INDEX idx_posts_feed, DROP INDEX idx_posts_author_feed;
ALTER TABLE comments DROP INDEX idx_comments_post_feed, DROP INDEX idx_comments_user;
ALTER TABLE likes DROP INDEX uk_likes_user_post, DROP INDEX idx_likes_post;
ALTER TABLE friends DROP INDEX uk_friends_pair, DROP INDEX idx_friends_followed_status;
ALTER TABLE messages DROP INDEX idx_messages_receiver_sent, DROP INDEX idx_messages_sender_sent;
ALTER TABLE company DROP INDEX idx_company_user;
ALTER TABLE jobs DROP INDEX idx_jobs_company;
ALTER TABLE postulations DROP INDEX uk_postulations_user_job, DROP INDEX idx_postulations_job_status;
ALTER TABLE work_stations DROP INDEX idx_work_stations_user;
ALTER TABLE profile_photos DROP INDEX uk_profile_photos_user;
-- El UNIQUE de user.name no se restaura (era un error de modelado). migration_audit se conserva.
