## Context

Estado esperado tras `fix-critical-risks`: JWT en cookie httpOnly, actor tomado del token, sin passwords en las respuestas, gateway separado, configuración por entorno.

Superficie que sigue abierta:

| Área | Situación |
|---|---|
| Autorización | Autenticado ≠ autorizado: no hay checks de propiedad en edit/delete de posts, comments, company, job, postulation, message, workstation, user |
| Media | multer sin `limits` ni `fileFilter`, nombre con extensión del cliente, `express.static` sin headers, subida sin auth |
| Headers | Ninguno configurado |
| Abuso | Sin rate limiting ni lockout |
| CSRF | Solo `SameSite=Lax` |
| Supply chain | Sin SCA, SAST, Dependabot ni escaneo de secretos; contraseña de MySQL en el historial |
| Exposición | El perfil público incluye el email del usuario |

## Goals / Non-Goals

**Goals:**
- Una verificación sistemática (auditoría) con evidencia y estado por hallazgo.
- Corregir los hallazgos altos y críticos dentro de este change.
- Automatizar la verificación para que no haya regresiones (CI + tests).

**Non-Goals:**
- Pentest externo profesional ni certificaciones.
- WAF, gestión de infraestructura o hardening de servidores de producción.
- MFA, OAuth y gestión avanzada de sesiones (revocación, refresh): quedan como hallazgos aceptados de riesgo medio si aplica.
- Cifrado en reposo de la base de datos.

## Decisions

### D1. Metodología de auditoría
- Checklist OWASP ASVS 4.0 L1 + Top 10 2025, recorrida servicio por servicio.
- Herramientas: la skill `cyber-neo` (SAST, secretos y configuración local) para un primer barrido, revisión manual de controllers/services y ZAP para la parte dinámica.
- Formato del hallazgo: `ID | Severidad (CVSS aprox.) | OWASP | Componente | Evidencia (request/response o línea) | Remediación | Estado | Test`.

### D2. Autorización en la capa de servicio
- Checks explícitos en los services (`assertOwner(resource.getAuthorId())`) más `@PreAuthorize("hasRole('ADMIN')")` para operaciones de admin. `@EnableMethodSecurity` está activo.
- Se prefiere el check en el service, y no solo `@PreAuthorize` con SpEL, porque necesita cargar el recurso. Se centraliza en un `OwnershipGuard` para no repetir lógica.
- Los recursos privados responden 404 ante acceso ajeno (no se revela su existencia). Los públicos con mutación responden 403.
- **Alternativa**: ACLs de Spring Security. Son excesivas para este dominio.

### D3. Media: validar por contenido y re-encodear
- `multer` con `limits: { fileSize: 5MB, files: 1 }` y `memoryStorage`. Se valida con `file-type` (magic bytes) y se re-encodea con `sharp` (lo que elimina EXIF y neutraliza polyglots). Se escribe `UUID.<ext>` en `UPLOAD_DIR`.
- Auth: el filtro JWT del gateway sobre todo `/upload/**` ya lo introduce `fix-critical-risks`. Aquí se **verifica** (tests de subida y lectura anónimas → 401) y se completa la validación del contenido.
- Servido: `express.static` con `index: false`, `dotfiles: 'deny'` y `setHeaders` (`nosniff`, `Cache-Control`).
- **Alternativa**: validar el JWT en Node. Duplica la lógica en otro lenguaje.

### D4. Headers en el gateway
- Un filtro global de Spring Cloud Gateway (`SecureHeaders` + CSP personalizada). La CSP se ajusta a Astro: `script-src 'self'`; si Astro inyecta scripts inline de hidratación se usa hash o nonce y no `unsafe-inline`; `img-src 'self' data:`; `connect-src 'self' <API>`.
- HSTS solo con el perfil `prod`.

### D5. CSRF por validación de Origin
- Un filtro en la API rechaza las mutaciones si `Origin`/`Referer` no está en `CORS_ORIGINS`. Combinado con `SameSite=Lax`, cubre los casos clásicos sin estado adicional.
- **Alternativa**: token double-submit (`CsrfTokenRepository` con cookie). Se adopta si la auditoría encuentra un caso no cubierto (p. ej. subdominios bajo control de terceros).

### D6. Rate limiting con Bucket4j en memoria
- Filtro en la API para `/api/auth/**`: bucket por IP (10/min) y contador de fallos por email (5 → lockout de 15 min) en Caffeine.
- En memoria basta mientras haya una sola instancia. Si se escala horizontalmente, se migra a Redis (anotado como riesgo).
- La IP real se toma de `X-Forwarded-For` **solo** si viene del gateway (lista de proxies de confianza).

### D7. Pipeline de seguridad
```
security.yml   (PR + cron semanal)
 ├─ gitleaks           (PR: diff · cron: --log-opts=--all)
 ├─ codeql             (java, javascript-typescript)
 ├─ semgrep            (p/owasp-top-ten, p/java, p/typescript)
 ├─ dependency-check   (maven; fail on CVSS ≥ 7, suppressions.xml con justificación)
 └─ npm audit          (--audit-level=high por proyecto)
dast.yml       (push main + cron)
 └─ compose up (stack E2E) → login semilla → zap-baseline con contexto autenticado → artifact
dependabot.yml (maven, npm×3, docker, github-actions; semanal)
```
- Los hallazgos de la IA de triage (`add-e2e-ai-ci`) no aplican aquí. Los reportes se revisan a mano y la auditoría es la fuente de verdad.

## Risks / Trade-offs

- [Una CSP estricta rompe la hidratación de Astro o los estilos de Tailwind] → Empezar en modo `Content-Security-Policy-Report-Only` y pasar a enforce tras validar con E2E.
- [Falsos positivos en SCA y SAST bloquean PRs] → Suppressions con justificación y fecha de revisión. Solo bloquea severidad alta o crítica.
- [El rate limiting en memoria no se comparte entre instancias] → Aceptado mientras haya una sola instancia; documentado.
- [`sharp` añade una dependencia nativa al servicio de media] → Se usa una imagen Docker oficial `node:22-slim`, compatible con los binarios precompilados de sharp.
- [El gateway valida JWT solo para `/upload`, lo que duplica la clave] → Mismo `JWT_SECRET` por env. La validación es solo de firma y expiración.
- [Quitar el email del perfil público rompe la UI] → Se ajusta el frontend en este mismo change.

## Migration Plan

1. Pipeline de escaneo en modo reporte (no bloqueante) + primera auditoría.
2. Corregir los hallazgos críticos y altos: autorización, media, rate limit, headers en Report-Only.
3. Tests de regresión por hallazgo.
4. CSP en enforce, jobs de seguridad bloqueantes y branch protection actualizada.
5. Cerrar la auditoría con el estado final y publicar `SECURITY.md`.

## Resolved Decisions

- Roles: exactamente `USER`, `COMPANY_ADMIN` y `ADMIN`. Solo un `COMPANY_ADMIN` puede crear empresas; el dueño de una empresa (siendo `COMPANY_ADMIN`) gestiona sus empleos y postulaciones; `ADMIN` puede moderar todo.
- Toda la API y la media requieren sesión (red cerrada). ZAP corre siempre autenticado, y además se verifica que sin sesión todo responde 401.

## Open Questions

- ¿Hay un entorno desplegado (staging) contra el cual correr ZAP además del stack de CI?
- ¿Se quiere ocultar el email en todos los perfiles ajenos, o permitirlo como opción del usuario? Por defecto se oculta.
