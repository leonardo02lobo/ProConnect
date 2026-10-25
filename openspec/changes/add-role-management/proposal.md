## Why

Los roles `USER`, `COMPANY_ADMIN` y `ADMIN` existen y se aplican, pero no hay forma de **asignarlos**. El registro siempre crea `USER` y `AdminBootstrap` solo crea el primer `ADMIN`, así que nadie puede llegar a ser `COMPANY_ADMIN`. Eso deja inutilizable el flujo de empresas → empleos → postulaciones → correos, que es un requisito de producto. La revisión de `AdminBootstrap` encontró además tres debilidades:
- El password del bootstrap queda en las variables de entorno.
- Si el `userName` derivado del email choca con uno existente, la API no arranca.
- La política de password es solo de longitud.

## What Changes

- Nuevo endpoint `PUT /api/admin/users/{id}/role` (solo `ADMIN`) para asignar `USER`, `COMPANY_ADMIN` o `ADMIN`, y `GET /api/admin/users?q=&page=&size=` para listar usuarios con su rol.
- Salvaguardas:
  - Un ADMIN no puede quitarse su propio rol de ADMIN.
  - No se puede degradar al último ADMIN (siempre queda al menos uno).
  - Cada cambio queda en una auditoría (`role_changes`: quién, a quién, de → a, cuándo).
- El cambio de rol tiene **efecto inmediato** en las sesiones abiertas: el filtro JWT resuelve el rol vigente desde la base (cache corta con invalidación al cambiar), en lugar de confiar solo en el claim.
- `AdminBootstrap` endurecido:
  - acepta `BOOTSTRAP_ADMIN_PASSWORD_FILE` (Docker secrets) como alternativa a la variable;
  - acepta `BOOTSTRAP_ADMIN_USERNAME` explícito y, si no se da, añade un sufijo numérico si el `userName` derivado ya existe;
  - valida la fortaleza del password (≥ 12 caracteres, mezcla de clases) y lo documenta;
  - recomienda retirar las variables después del primer arranque.
- Frontend: página `/admin/users`, visible solo para ADMIN, para buscar usuarios y cambiar su rol, con un enlace en el menú del header.
- E2E: el ADMIN promueve a un usuario a `COMPANY_ADMIN` → ese usuario crea una empresa y un empleo → otro usuario se postula → el dueño acepta → Mailpit recibe el correo.

## Capabilities

### New Capabilities
- `role-management`: asignación de roles por un ADMIN, salvaguardas, auditoría y efecto inmediato sobre las sesiones.
- `admin-bootstrap`: alta segura del primer ADMIN por configuración.

### Modified Capabilities
<!-- No hay specs archivadas en openspec/specs/ todavía. -->

## Impact

- **backProconnect**:
  - nuevos `AdminController`, `RoleService` y entidad `RoleChange` (tabla `role_changes`);
  - cambios en `JwtAuthFilter` (rol desde BD con cache), `AdminBootstrap` y `UserRepository` (conteo de ADMINs);
  - tests nuevos.
- **FrontedProConnect**: página `/admin/users`, enlace condicional en el `Header`, servicio `admin.ts`.
- **e2e**: flujo completo de empresa con promoción de rol.
- **Coordinación con `restructure-models-performance`**: `role_changes` se crea aquí con `ddl-auto=update`. Si perf ya introdujo Flyway cuando se mergee, se añade la migración `V*_role_changes.sql`.
