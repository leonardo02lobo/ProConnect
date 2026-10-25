-- V2.1: tipos de dominio (design D6).
--  * Fechas: columnas `created_at` / `sent_at` DATETIME(6) en UTC (java.time.Instant,
--    hibernate.jdbc.time_zone=UTC). Las filas legacy sin fecha (NULL) pasan a 1970-01-01 (quedan al
--    final del feed) y la columna pasa a NOT NULL. Los índices de V2 siguen a la columna renombrada.
--  * Estados y roles como texto validado: Friend.status (PENDING/ACCEPTED/REJECTED), User.role
--    (USER/COMPANY_ADMIN/ADMIN) y Postulation.status, con CHECK en la base. Normalización: mayúsculas y
--    sin espacios; un rol desconocido pasa a USER (mínimo privilegio, como Role.fromString) y un estado
--    de amistad desconocido a PENDING (el destinatario puede volver a decidir). Recuento en migration_audit.

INSERT INTO migration_audit (version, check_name, affected_rows)
SELECT 'V2.1', 'null_dates', (SELECT COUNT(*) FROM posts WHERE `date` IS NULL)
      + (SELECT COUNT(*) FROM comments WHERE `date` IS NULL)
      + (SELECT COUNT(*) FROM likes WHERE `date` IS NULL)
      + (SELECT COUNT(*) FROM jobs WHERE `date` IS NULL)
      + (SELECT COUNT(*) FROM postulations WHERE `date` IS NULL)
      + (SELECT COUNT(*) FROM messages WHERE `timestamp` IS NULL)
UNION ALL SELECT 'V2.1', 'friend_status_normalized', COUNT(*) FROM friends
    WHERE BINARY status NOT IN ('PENDING', 'ACCEPTED', 'REJECTED')
UNION ALL SELECT 'V2.1', 'friend_status_unknown', COUNT(*) FROM friends
    WHERE UPPER(TRIM(status)) NOT IN ('PENDING', 'ACCEPTED', 'REJECTED')
UNION ALL SELECT 'V2.1', 'user_role_normalized', COUNT(*) FROM `user`
    WHERE BINARY role NOT IN ('USER', 'COMPANY_ADMIN', 'ADMIN')
UNION ALL SELECT 'V2.1', 'user_role_unknown', COUNT(*) FROM `user`
    WHERE UPPER(TRIM(role)) NOT IN ('USER', 'COMPANY_ADMIN', 'ADMIN')
UNION ALL SELECT 'V2.1', 'postulation_status_unknown', COUNT(*) FROM postulations
    WHERE UPPER(TRIM(status)) NOT IN ('PENDING', 'ACCEPTED', 'REJECTED');

-- Fechas
UPDATE posts SET `date` = '1970-01-01 00:00:00' WHERE `date` IS NULL;
UPDATE comments SET `date` = '1970-01-01 00:00:00' WHERE `date` IS NULL;
UPDATE likes SET `date` = '1970-01-01 00:00:00' WHERE `date` IS NULL;
UPDATE jobs SET `date` = '1970-01-01 00:00:00' WHERE `date` IS NULL;
UPDATE postulations SET `date` = '1970-01-01 00:00:00' WHERE `date` IS NULL;
UPDATE messages SET `timestamp` = '1970-01-01 00:00:00' WHERE `timestamp` IS NULL;

ALTER TABLE posts CHANGE `date` created_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6);
ALTER TABLE comments CHANGE `date` created_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6);
ALTER TABLE likes CHANGE `date` created_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6);
ALTER TABLE jobs CHANGE `date` created_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6);
ALTER TABLE postulations CHANGE `date` created_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6);
ALTER TABLE messages CHANGE `timestamp` sent_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6);

-- Enums
UPDATE friends SET status = UPPER(TRIM(status));
UPDATE friends SET status = 'PENDING' WHERE status NOT IN ('PENDING', 'ACCEPTED', 'REJECTED');
UPDATE `user` SET role = UPPER(TRIM(role));
UPDATE `user` SET role = 'USER' WHERE role NOT IN ('USER', 'COMPANY_ADMIN', 'ADMIN');
UPDATE postulations SET status = UPPER(TRIM(status));
UPDATE postulations SET status = 'PENDING' WHERE status NOT IN ('PENDING', 'ACCEPTED', 'REJECTED');

ALTER TABLE friends
    MODIFY status varchar(16) NOT NULL,
    ADD CONSTRAINT ck_friends_status CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED'));
ALTER TABLE `user`
    MODIFY role varchar(16) NOT NULL DEFAULT 'USER',
    ADD CONSTRAINT ck_user_role CHECK (role IN ('USER', 'COMPANY_ADMIN', 'ADMIN'));
ALTER TABLE postulations
    ADD CONSTRAINT ck_postulations_status CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED'));
