## Why

`fix-critical-risks` introduce autenticación real, pero autenticar no es autorizar. Hoy cualquier usuario autenticado podría editar o borrar posts, comentarios, empresas o empleos ajenos (IDOR). El servicio de media acepta cualquier archivo sin límite de tamaño ni validación de tipo, y lo sirve desde el mismo origen. Faltan headers de seguridad, rate limiting en el login y escaneos de dependencias, secretos y código. Además, la contraseña de MySQL quedó en el historial de git. Hay que verificar de forma sistemática y repetible que la web es segura una vez aplicada la primera mejora, y dejar esa verificación automatizada en CI.

## What Changes

- **Auditoría de seguridad** documentada (`docs/security/audit-<fecha>.md`) contra OWASP Top 10 2025 y ASVS nivel 1: hallazgos con severidad, evidencia, remediación y estado.
- **Autorización por propiedad**: solo el autor puede editar o borrar sus posts y comentarios, solo el dueño de una empresa puede gestionarla junto con sus empleos y postulaciones, y solo los participantes pueden ver una conversación. Se respeta `role` (ADMIN) donde aplique. **BREAKING** para clientes que hoy mutan recursos ajenos.
- **Endurecimiento del servicio de media**: allowlist de tipos (JPEG/PNG/WebP/GIF) validada por magic bytes, tamaño máximo (5 MB), nombre de archivo generado en el servidor, re-encode o strip de metadatos EXIF, subida solo autenticada (el gateway la protege) y servido con `Content-Type` correcto, `X-Content-Type-Options: nosniff` y `Content-Disposition` seguro.
- **Headers HTTP** en el gateway: CSP, HSTS (fuera de dev), `X-Frame-Options`/`frame-ancestors`, `Referrer-Policy` y `Permissions-Policy`.
- **Protección de abuso**: rate limiting en `POST /api/auth/login` y `/register` (por IP y por cuenta), bloqueo temporal tras fallos repetidos y límites en la subida de imágenes.
- **CSRF**: verificación de la estrategia `SameSite` + validación de `Origin` en mutaciones (o double-submit token si el análisis lo exige).
- **Escaneos automatizados en CI**:
  - SCA: OWASP Dependency-Check (Maven), `npm audit` y Dependabot.
  - SAST: CodeQL (Java + JS/TS) y Semgrep.
  - Secretos: gitleaks sobre el diff de cada PR y sobre todo el historial en el cron semanal.
  - DAST: OWASP ZAP baseline contra el stack E2E, autenticado con el usuario semilla.
- **Tests de seguridad de regresión**: suite que prueba IDOR, uploads maliciosos, headers, rate limit y ausencia de datos sensibles en respuestas, integrada en `ci`/`e2e`.

## Capabilities

### New Capabilities
- `access-control`: autorización por propiedad y rol sobre todos los recursos mutables y privados.
- `media-upload-security`: validación, almacenamiento y servido seguro de imágenes subidas por usuarios.
- `http-security-hardening`: headers de seguridad, CSRF, rate limiting y bloqueo por fuerza bruta.
- `security-scanning`: escaneos SCA, SAST, secretos y DAST automatizados en CI, más la auditoría documentada.

### Modified Capabilities
<!-- Sin specs archivadas aún. Cuando `user-auth` (fix-critical-risks) esté en openspec/specs/, el rate limiting del login puede expresarse como delta ADDED sobre `user-auth`. -->

## Impact

- **backProconnect**: autorización (`@PreAuthorize` o checks en services) en Posts, Comments, Companies, Jobs, Postulations, Messages, Friends, WorkStations y Users; validación de `Origin`; rate limiting (Bucket4j).
- **gateway**: filtros de headers de seguridad, protección de `/upload/**` (requiere sesión válida) y, opcionalmente, rate limiting global.
- **NodeTsPosts**: validación de archivos (`file-type`), límites de multer y headers al servir.
- **CI**: workflows `security.yml` (por PR y cron semanal) y `dast.yml`, `.github/dependabot.yml`, configuración de CodeQL, Semgrep y gitleaks.
- **Docs**: `docs/security/audit-*.md`, `SECURITY.md` con la política de reporte.
- **Depende de** `fix-critical-risks` (obligatorio) y de `add-e2e-ai-ci` (stack Compose y pipeline). Se puede ejecutar antes o después de `cleanup-codebase`/`restructure-models-performance`, pero conviene repetir los escaneos tras cada uno.
