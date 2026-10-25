-- Rollback MANUAL de V5: recrea profile_photos (como tras V2) desde user.avatar_url/cover_url.
-- Los ids de las filas originales no se recuperan (restaurar el backup si hacen falta).
CREATE TABLE profile_photos (
  id bigint NOT NULL AUTO_INCREMENT,
  photo_url varchar(255) DEFAULT NULL,
  user_id bigint NOT NULL,
  wall_photo_url varchar(255) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_profile_photos_user (user_id),
  CONSTRAINT fk_profile_photos_user FOREIGN KEY (user_id) REFERENCES `user` (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
INSERT INTO profile_photos (user_id, photo_url, wall_photo_url)
SELECT id, avatar_url, cover_url FROM `user` WHERE avatar_url IS NOT NULL OR cover_url IS NOT NULL;
