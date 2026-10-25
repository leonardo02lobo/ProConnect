# Política de seguridad

## Versiones soportadas

Solo se da soporte de seguridad a la rama `main` (último despliegue). No hay versiones LTS.

## Cómo reportar una vulnerabilidad

**No abras un issue público.** Usa el reporte privado de GitHub:
`Security` → `Report a vulnerability` en https://github.com/leonardo02lobo/ProConnect/security/advisories/new

Incluye, si puedes: componente afectado (`backProconnect`, `gateway`, `NodeTsPosts`, `FrontedProConnect`, CI),
pasos para reproducir (petición/respuesta), impacto estimado y versión/commit.

Compromisos de respuesta (proyecto mantenido por una persona, en la medida de lo posible):

| Paso | Plazo objetivo |
|---|---|
| Acuse de recibo | 3 días hábiles |
| Evaluación inicial y severidad | 7 días |
| Corrección de crítica/alta | 30 días |
| Divulgación coordinada | tras publicar el parche, con crédito al reportante si lo desea |

## Alcance

Dentro: la API (`/api/**`), el gateway, el servicio de media (`/upload/**`), el frontend web y los workflows de
`.github/`. Fuera: el backend legacy `BackendProConnect/` (no desplegado, se retira), ataques de denegación de servicio
volumétricos, ingeniería social, y hallazgos que requieran un dispositivo o navegador ya comprometido.

No pruebes contra datos de otros usuarios reales: usa cuentas propias o el stack local
(`docker-compose -f docker-compose.e2e.yml up`).

## Controles existentes

Resumen en [`docs/security/audit-2026-10-24.md`](docs/security/audit-2026-10-24.md): autorización por propiedad y rol en
la capa de servicio, CSRF por validación de `Origin`, rate limiting y bloqueo de login, cookie de sesión `HttpOnly`,
CSP en enforce, validación de imágenes por contenido, y escaneos automáticos (gitleaks, CodeQL, Semgrep,
Dependency-Check, npm audit, OWASP ZAP) en `.github/workflows/security.yml` y `dast.yml`.
