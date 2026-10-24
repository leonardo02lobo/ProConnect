## Why

El código acumula deuda que hace lento y riesgoso cada cambio:
- Los services devuelven `"Error: ..."` como `String` y los controllers lo responden con HTTP 200. Hay 22 casos.
- 13 bloques `catch (Exception)` se tragan los errores.
- Hay 40 métodos públicos en PascalCase y chequeos sin sentido como `Long.valueOf(Id) == null` (14 casos).
- Lógica duplicada: dos `GetAllPosts` casi idénticos con mapeo manual de `Object[]`.
- Hay un NPE latente en `PostsService.convertToDto`.
- El frontend llena la consola con `console.log` (18 casos), recarga la página después de cada comentario y tiene invertidas las páginas `SignIn` y `SignUp`.
- Además conviven un backend Express legacy y uno Spring con el mismo dominio.

Limpiarlo ahora, con E2E como red de seguridad, deja una base consistente antes de reestructurar modelos.

## What Changes

- **API errors**: manejo centralizado con `@RestControllerAdvice` y excepciones de dominio (`NotFoundException`, `ConflictException`, `ValidationException`, `ForbiddenException`). Las respuestas de error siguen RFC 7807 (`application/problem+json`) y los códigos HTTP son correctos (400/401/403/404/409/500).
- **BREAKING** Los services dejan de devolver mensajes `String` y pasan a devolver DTOs o lanzar excepciones. Las respuestas de éxito devuelven el recurso creado o actualizado (201/200) en vez de textos como `"Posts Add Successufly"`.
- **Convenciones Java**: métodos en camelCase, campos `Id` → `id`, inyección por constructor final, se eliminan los chequeos redundantes y se ponen `@Valid` y Bean Validation en los DTOs de entrada.
- **Duplicación**: un solo método de feed, con el usuario actual opcional. El mapeo de `Object[]` se reemplaza por proyecciones tipadas (interfaces o records); la optimización de esas queries va en `restructure-models-performance`.
- **Rutas REST coherentes**: se eliminan los sufijos verbales (`/create`, `/edit/{id}`, `/delete/{id}`) en favor de verbos HTTP sobre recursos (`POST /api/posts`, `PUT /api/posts/{id}`, `DELETE /api/posts/{id}`). `DELETE` deja de llevar body (`DELETE /api/posts/{postId}/likes`). **BREAKING** para el frontend, que se migra en el mismo change.
- **Correos de postulación en Spring**, que es un requisito de producto. `Postulation` gana `status` (PENDING/ACCEPTED/REJECTED). El dueño de la empresa resuelve con `POST /api/postulations/{id}/accept|reject`, y el postulante recibe el correo positivo o negativo con las mismas plantillas del legacy. El envío usa `spring-boot-starter-mail`, es asíncrono después del commit y tiene reintentos. SMTP por env, con Mailpit en dev y E2E. El destinatario se toma de la base de datos y ya no del body: el endpoint legacy `/api/Correos/*` permitía enviar correos a cualquier dirección.
- **Retiro del backend legacy**: una vez pasados los correos, se elimina `BackendProConnect/` (Express). Queda referenciado en el tag git `legacy-express-backend`. El resto de la funcionalidad exclusiva del legacy se inventaría en `docs/backlog-legacy.md`.
- **Frontend**:
  - Se corrigen nombres y rutas: `/login` y `/register` reemplazan a `SignUp` y `SignIn`, que están invertidas.
  - Se quitan los `console.log`.
  - Hay una capa `Service/` tipada por recurso; los componentes ya no hacen `fetch` directo.
  - El estado se actualiza de forma optimista, sin `window.location.reload()`.
  - Las props pasan a inglés consistente (`totalComments`).
- **Calidad automatizada**: Spotless (google-java-format) + Checkstyle en Maven, ESLint + Prettier en frontend y Node, y un `.editorconfig` raíz. Todo corre en el workflow `ci`.

## Capabilities

### New Capabilities
- `api-error-handling`: contrato uniforme de errores (problem+json) y códigos HTTP correctos en toda la API.
- `rest-api-conventions`: rutas y verbos REST coherentes y respuestas que devuelven recursos.
- `code-quality-standards`: formato, lint y convenciones verificadas automáticamente en CI, más el retiro del código muerto o legacy.
- `job-application-notifications`: estado de la postulación, resolución por el dueño de la empresa y correo al postulante (traído del legacy).

### Modified Capabilities
<!-- Sin specs en openspec/specs/. Si fix-critical-risks y add-e2e-ai-ci se archivan antes, ci-pipeline gana un requisito de lint: se añade como delta al momento de aplicar. -->

## Impact

- **backProconnect**: todos los controllers y services, DTOs de entrada (validación), nuevo paquete `exception/`, `pom.xml` (Spotless, Checkstyle).
- **FrontedProConnect**: renombrar páginas, `Service/*`, todos los componentes, ESLint/Prettier.
- **NodeTsPosts**: ESLint/Prettier y un script `dev` real (`tsx` o `ts-node`, hoy ejecuta `node ./index.ts`).
- **Eliminado**: `BackendProConnect/` completo.
- **E2E**: los tests de `add-e2e-ai-ci` se actualizan a las rutas nuevas (`/login`, `/register`) y deben quedar en verde al final.
- **Depende de** `fix-critical-risks`. Se recomienda aplicarlo después de `add-e2e-ai-ci`.
