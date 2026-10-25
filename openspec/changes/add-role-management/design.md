## Context

- Los roles se guardan como `String` en `user.role` y existe el enum `Role { USER, COMPANY_ADMIN, ADMIN }` con `Role.fromString`.
- `OwnershipGuard` (de verify-web-security) tiene `requireAnyRole`, `isAdmin`, `assertOwnerOrAdmin`, etc.
- `JwtAuthFilter` arma el `AuthPrincipal` con el claim `role` del JWT y `TokenDenylist` revoca tokens por `jti`.
- `AdminBootstrap` (de cleanup-codebase) crea el primer ADMIN desde `BOOTSTRAP_ADMIN_*`.
- No existe ningún endpoint para cambiar roles, así que `COMPANY_ADMIN` es inalcanzable.
- `restructure-models-performance` (fase 2) está introduciendo Flyway en paralelo, sobre otra rama.

## Goals / Non-Goals

**Goals:**
- Que un ADMIN pueda asignar roles con salvaguardas y auditoría.
- Que el cambio de rol tenga efecto inmediato y seguro.
- Que el flujo de empresas funcione de punta a punta (E2E).
- Un bootstrap del primer ADMIN robusto en operación.

**Non-Goals:**
- Permisos finos o roles personalizados.
- Invitaciones a empresas o equipos de varias personas por empresa.
- Panel de administración completo (moderación de contenido, métricas).
- Gestión de usuarios más allá del rol (bloquear o borrar cuentas desde admin).

## Decisions

### D1. Endpoints bajo `/api/admin/**`
- `AdminController` con `GET /api/admin/users` y `PUT /api/admin/users/{id}/role`. El check vive en el servicio (`guard.requireAnyRole(ADMIN)`), igual que el resto de la autorización, y además hay un `requestMatchers("/api/admin/**").hasRole("ADMIN")` en `SecurityConfig` como defensa en profundidad.
- **Alternativa**: `PATCH /api/users/{id}` con `role` en el body. Mezcla la edición de perfil (propia) con la administración (ajena) y complica el guard.

### D2. Rol efectivo desde la base, con cache
- `RoleResolver` con Caffeine (`expireAfterWrite=30s`, clave `userId`) consulta `user.role`. `JwtAuthFilter` usa el rol resuelto para las authorities y conserva el claim solo como fallback si el usuario no existe (en ese caso responde 401).
- `RoleService.changeRole` invalida la entrada de cache después del commit (`@TransactionalEventListener`).
- Coste: como mucho una query por usuario cada 30 s. Es despreciable frente al feed y no afecta a los umbrales de queries de perf, porque ocurre fuera de los repositorios medidos o cacheado. Hay que verificarlo con los tests de conteo de perf cuando se mergee.
- **Alternativa**: revocar todos los tokens del usuario al cambiar el rol (`tokensValidAfter`). Obliga a volver a iniciar sesión y también necesita consultar la base por petición.
- **Alternativa**: confiar en el claim hasta que expire. Una degradación tardaría hasta 24 h en aplicarse: inaceptable.

### D3. Salvaguardas transaccionales
- En `changeRole`: `SELECT ... FOR UPDATE` (`@Lock(PESSIMISTIC_WRITE)`) sobre el usuario objetivo, y `countByRole(ADMIN)` dentro de la misma transacción. Si el resultado deja 0 ADMIN, o el actor es el objetivo y pierde ADMIN, se responde 409 con `ConflictException`.
- Si el rol nuevo es igual al actual, se responde 200 sin auditoría.

### D4. Auditoría
- Entidad `RoleChange(id, actorId, targetUserId, fromRole, toRole, changedAt)`. Solo `INSERT` y sin endpoint de modificación. El log emite un INFO con los IDs, sin emails.

### D5. AdminBootstrap
- Propiedades: `proconnect.bootstrap.admin.{email,password,password-file,username,name}`.
- Se resuelve primero `password-file` y se aplica `strip()`. Con ambas definidas se lanza `IllegalStateException`.
- Validador `PasswordPolicy.bootstrap()`: ≥ 12 caracteres y 3 de 4 clases. El registro normal mantiene ≥ 8, porque cambiarlo es otro asunto.
- userName: el explícito se usa tal cual (si colisiona, `IllegalStateException`). El derivado prueba `base`, `base1`, `base2`… hasta encontrar uno libre.
- La compose de E2E pasa a usar un password que cumple la política nueva.

### D6. Frontend
- `src/pages/admin/users.astro`. El middleware de Astro consulta `/api/auth/me` (ya existe para la sesión) y redirige a `/` si `role !== "ADMIN"`; el backend sigue siendo la barrera real.
- Una isla React `AdminUsers.tsx` con buscador, tabla y `select` de rol, y servicio `src/Service/admin.ts` sobre `apiFetch`.
- El enlace "Administración" en el menú del `Header` solo se muestra si el usuario es ADMIN.

## Risks / Trade-offs

- [La cache de rol permite hasta 30 s de rol antiguo en otra instancia] → La invalidación es local y hay una sola instancia. Si se escala, se baja el TTL o se usa una cache distribuida (documentado).
- [Conflicto con Flyway de perf] → `role_changes` se crea con `ddl-auto=update` en esta rama. Al mergear con perf se añade `V*_role_changes.sql` y lo valida `ddl-auto=validate`. Queda registrado como tarea de integración.
- [Conflicto en `JwtAuthFilter` y `SecurityConfig` con upgrade-spring-boot-4] → El orden de merge es role-management → perf → boot4; boot4 adapta el código ya integrado.
- [Política del bootstrap más estricta que la del registro] → Es intencional: es la cuenta más privilegiada.

## Migration Plan

1. Backend: `RoleChange`, `RoleService`, `RoleResolver`, `AdminController` + tests.
2. `AdminBootstrap` endurecido + tests.
3. Frontend: página admin + enlace.
4. E2E del flujo de empresa (con Mailpit) y E2E de permisos de la página admin.
5. CI local verde → push → PR draft apilado sobre `leonardo02lobo/cleanup-codebase`.
