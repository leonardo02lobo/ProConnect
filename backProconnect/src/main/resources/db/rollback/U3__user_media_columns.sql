-- Rollback MANUAL de V3 (solo mientras profile_photos exista, antes de V5).
-- Copia de vuelta los cambios hechos con la versión nueva y quita las columnas.
INSERT INTO profile_photos (user_id, photo_url, wall_photo_url)
SELECT u.id, u.avatar_url, u.cover_url FROM `user` u
 WHERE (u.avatar_url IS NOT NULL OR u.cover_url IS NOT NULL)
ON DUPLICATE KEY UPDATE photo_url = VALUES(photo_url), wall_photo_url = VALUES(wall_photo_url);
ALTER TABLE `user` DROP COLUMN avatar_url, DROP COLUMN cover_url;
