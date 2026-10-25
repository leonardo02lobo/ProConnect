-- V5 (contract): la app ya no lee ni escribe profile_photos desde V3 (avatar/portada en `user`).
-- Aplicar solo cuando todos los entornos corren una versión >= V3 estable (docs/db/migrations.md).
-- Irreversible sin backup: U5 recrea la tabla desde las columnas de `user`, sin los ids originales.
INSERT INTO migration_audit (version, check_name, affected_rows)
SELECT 'V5', 'profile_photos_dropped', COUNT(*) FROM profile_photos;

DROP TABLE profile_photos;
