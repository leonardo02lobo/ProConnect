-- V6 (add-role-management): auditoría de cambios de rol (entidad Entity/RoleChange).
-- Sin FKs a `user` a propósito: la auditoría sobrevive al borrado del usuario (actor u objetivo).
-- IF NOT EXISTS: una base de desarrollo de la rama de roles (ddl-auto=update) ya puede tenerla,
-- con la misma definición que genera Hibernate para MySQL 8.4.
CREATE TABLE IF NOT EXISTS role_changes (
  id BIGINT NOT NULL AUTO_INCREMENT,
  actor_id BIGINT NOT NULL,
  target_user_id BIGINT NOT NULL,
  from_role VARCHAR(32) NOT NULL,
  to_role VARCHAR(32) NOT NULL,
  changed_at DATETIME(6) NOT NULL,
  PRIMARY KEY (id),
  KEY idx_role_changes_target (target_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
