-- Rollback MANUAL de V6: borra la auditoría de cambios de rol (hacer backup antes si se necesita).
DROP TABLE IF EXISTS role_changes;
