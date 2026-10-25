## 1. Backend: gestión de roles

- [x] 1.1 Entidad `RoleChange` y `RoleChangeRepository` (solo inserción)
- [x] 1.2 `UserRepository.countByRole` y búsqueda paginada por `q` (name/userName/email); lock pesimista para el cambio de rol
- [x] 1.3 `RoleService.changeRole(targetId, role)`: guard ADMIN, validación de rol (400), salvaguardas de auto-degradación y último ADMIN (409), auditoría e invalidación de cache tras el commit
- [x] 1.4 `AdminController`: `GET /api/admin/users` (paginado, máx. 100) y `PUT /api/admin/users/{id}/role`; `/api/admin/**` con `hasRole('ADMIN')` en `SecurityConfig`
- [x] 1.5 `RoleResolver` (Caffeine 30 s) y uso en `JwtAuthFilter` para el rol efectivo
- [x] 1.6 Tests: promoción, 403 para no-admin, 400 por rol inválido, 409 por auto-degradación y por último ADMIN, auditoría, efecto inmediato con sesión abierta, y que `UnauthenticatedSweepTests` cubre las rutas nuevas

## 2. AdminBootstrap

- [x] 2.1 Soportar `BOOTSTRAP_ADMIN_PASSWORD_FILE` (con error si también está `BOOTSTRAP_ADMIN_PASSWORD`)
- [x] 2.2 `BOOTSTRAP_ADMIN_USERNAME` explícito y sufijo numérico en el userName derivado
- [x] 2.3 Política del password del bootstrap (≥ 12 caracteres, 3 de 4 clases) sin mostrarlo en el log
- [x] 2.4 Actualizar `.env.example`, `application.yml`, `docker-compose.e2e.yml` (password válido) y la sección del README sobre retirar las variables tras el primer arranque
- [x] 2.5 Tests: password desde archivo, configuración ambigua, password débil, colisión de userName derivado y explícito

## 3. Frontend

- [ ] 3.1 Servicio `src/Service/admin.ts` (listar y cambiar rol) sobre `apiFetch`
- [ ] 3.2 Página `src/pages/admin/users.astro` + isla `AdminUsers.tsx` (buscador, tabla, select de rol, mensajes de error problem+json), con `data-testid`
- [ ] 3.3 El middleware redirige a `/` a los no-ADMIN en `/admin/**`
- [ ] 3.4 Enlace "Administración" en el menú del `Header` solo para ADMIN

## 4. E2E

- [ ] 4.1 `admin.spec.ts`: el ADMIN (bootstrap de E2E) promueve a un usuario; un USER no ve el enlace y no accede a `/admin/users`
- [ ] 4.2 `company-flow.spec.ts`: COMPANY_ADMIN crea empresa y empleo → otro usuario se postula → el dueño acepta → Mailpit recibe el correo
- [ ] 4.3 Suite completa verde dos veces (`--workers=4`)

## 5. Integración y entrega

- [ ] 5.1 Spotless, Checkstyle, ESLint y Prettier verdes; `make ci-local` y `make lint-workflows`
- [ ] 5.2 Push y PR draft apilado sobre `leonardo02lobo/cleanup-codebase`; verificar los checks en GitHub
- [ ] 5.3 Dejar nota en BOARD para la integración con perf (migración Flyway de `role_changes`, conteo de queries con `RoleResolver`)
