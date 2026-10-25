-- V3 (expand): avatar y portada pasan a columnas de `user` (relación 1:1 que se leía en cada
-- consulta con un JOIN/subselect a profile_photos). Se copian desde profile_photos (una fila por
-- usuario tras V2). La tabla vieja se conserva hasta V5 (contract), cuando ningún código la lee.
ALTER TABLE `user`
    ADD COLUMN avatar_url varchar(255) NULL,
    ADD COLUMN cover_url varchar(255) NULL;

UPDATE `user` u
JOIN profile_photos p ON p.user_id = u.id
SET u.avatar_url = p.photo_url, u.cover_url = p.wall_photo_url;

INSERT INTO migration_audit (version, check_name, affected_rows)
SELECT 'V3', 'user_media_copied', COUNT(*) FROM `user` u JOIN profile_photos p ON p.user_id = u.id
UNION ALL
SELECT 'V3', 'user_media_mismatch', COUNT(*) FROM `user` u JOIN profile_photos p ON p.user_id = u.id
 WHERE NOT (u.avatar_url <=> p.photo_url) OR NOT (u.cover_url <=> p.wall_photo_url);
