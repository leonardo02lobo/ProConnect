## 1. Pipeline de escaneo (modo reporte)

- [x] 1.1 Crear `.github/dependabot.yml` (maven ×2, npm ×3, docker, github-actions; semanal)
- [x] 1.2 Crear `.github/workflows/security.yml` con gitleaks, CodeQL (java y js/ts), Semgrep, OWASP Dependency-Check y `npm audit`, sin bloquear todavía
- [x] 1.3 Crear `.github/workflows/dast.yml`: stack E2E + login semilla + ZAP baseline autenticado, con reporte como artifact
- [x] 1.4 Ejecutar gitleaks sobre todo el historial y registrar en la allowlist la contraseña de MySQL ya rotada

## 2. Auditoría inicial

- [x] 2.1 Ejecutar un barrido con la skill `cyber-neo` sobre el repo
- [x] 2.2 Revisar a mano cada controller y service con el checklist ASVS L1 / OWASP Top 10 2025
- [x] 2.3 Consolidar los resultados de las herramientas y la revisión manual en `docs/security/audit-<fecha>.md` (ID, severidad, OWASP, evidencia, remediación, estado)

## 3. Autorización

- [x] 3.1 Habilitar `@EnableMethodSecurity` y crear `OwnershipGuard`
- [x] 3.2 Checks de autor en edit/delete de posts y comentarios (403; el ADMIN puede moderar)
- [x] 3.3 Checks de propietario en companies, jobs y en la vista o resolución de postulaciones; solo `COMPANY_ADMIN` crea empresas
- [x] 3.3b Test que recorre todos los endpoints `/api/**` sin sesión y espera 401 (salvo login, registro y logout)
- [x] 3.4 Postulaciones y mensajes privados: 404 ante acceso ajeno
- [x] 3.5 Users, fotos y workstations: solo el propio usuario o ADMIN; solicitudes de amistad: solo el destinatario acepta o rechaza
- [x] 3.6 Quitar el email de las vistas públicas de perfil, feed y comentarios, y ajustar el frontend

## 4. Media

- [x] 4.1 Verificar el filtro JWT del gateway en `/upload/**` (introducido en `fix-critical-risks`): subida y lectura anónimas → 401
- [x] 4.2 Multer con `memoryStorage`, `limits` de 5 MB y 1 archivo, y manejo de errores 413
- [x] 4.3 Validación por magic bytes (`file-type`) con allowlist JPEG/PNG/WebP/GIF y 415 en otro caso
- [x] 4.4 Re-encode con `sharp` (strip de EXIF) y nombre `UUID.<ext>` en `UPLOAD_DIR`
- [x] 4.5 `express.static` con `index: false`, `dotfiles: 'deny'`, `nosniff` y `Cache-Control`

## 5. Hardening HTTP

- [x] 5.1 Filtro de headers de seguridad en el gateway (CSP en Report-Only, nosniff, Referrer-Policy, Permissions-Policy, frame-ancestors; HSTS en `prod`)
- [x] 5.2 Filtro de validación de `Origin`/`Referer` para mutaciones en la API
- [x] 5.3 Rate limiting con Bucket4j + Caffeine en `/api/auth/**` (10/min por IP, lockout de 15 min tras 5 fallos por email, 429 + `Retry-After`) y proxies de confianza para `X-Forwarded-For`
- [x] 5.4 Revisar los logs: sin passwords ni tokens; login fallido con email e IP
- [x] 5.5 Verificar atributos de la cookie de sesión (`HttpOnly`, `SameSite`, `Secure` en prod)

## 6. Tests de regresión de seguridad

- [x] 6.1 Tests de integración de IDOR por recurso (A intenta mutar o leer recursos de B → 403/404)
- [x] 6.2 Tests de media: HTML disfrazado (415), SVG (415), 12 MB (413), nombre con traversal, EXIF eliminado y subida anónima (401)
- [x] 6.3 Tests de headers y CSRF (`Origin` ajeno → 403)
- [x] 6.4 Tests de rate limit y lockout (429)
- [x] 6.5 Test que recorre las respuestas de los endpoints públicos y falla si aparecen `password`, `token` o email de terceros
- [x] 6.6 E2E: `document.cookie` no expone la sesión y la CSP no rompe la app

## 7. Cierre

- [x] 7.1 Pasar la CSP a enforce tras E2E verdes
- [ ] 7.2 Volver bloqueantes los jobs de seguridad (alto/crítico) y agregarlos como checks requeridos en `main` — jobs ya bloqueantes (`BLOCKING=true` en security.yml, dast.yml falla en alto); PENDIENTE DEL USUARIO: marcarlos como checks requeridos en la branch protection de `main`
- [x] 7.3 Actualizar la auditoría con el estado final de cada hallazgo y su test asociado
- [x] 7.4 Publicar `SECURITY.md` (política de reporte y alcance)
