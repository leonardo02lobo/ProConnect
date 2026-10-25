-- Rollback MANUAL de V4: la versión anterior calcula los contadores con COUNT(*).
ALTER TABLE posts DROP COLUMN like_count, DROP COLUMN comment_count;
