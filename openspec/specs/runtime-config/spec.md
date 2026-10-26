# runtime-config Specification

## Purpose
Configuración en tiempo de ejecución por variables de entorno, sin secretos en el código.
## Requirements
### Requirement: Secretos fuera del código
Ningún servicio MUST contener en archivos versionados credenciales de base de datos, secretos JWT ni otras claves. Todos MUST leerse de variables de entorno.

#### Scenario: Arranque sin secreto JWT
- **WHEN** la API core arranca sin la variable `JWT_SECRET` definida
- **THEN** el arranque falla con un mensaje claro, en vez de usar un valor por defecto embebido

#### Scenario: Revisión del repositorio
- **WHEN** se buscan en los archivos versionados las cadenas de configuración `spring.datasource.password=` o `password:` con valores literales
- **THEN** solo aparecen referencias a variables de entorno (`${DB_PASSWORD}`) o placeholders en archivos `.env.example`

### Requirement: Puertos y URLs configurables sin conflictos
Cada servicio MUST tomar su puerto de una variable de entorno, con defaults distintos entre sí (gateway 8080, API 8081, media 3001, frontend 4321). El frontend MUST tomar la URL del backend de `PUBLIC_API_URL`.

#### Scenario: Levantar todo el stack localmente
- **WHEN** se arrancan gateway, API core, servicio de media y frontend con sus valores por defecto
- **THEN** todos inician sin error `EADDRINUSE` ni `Port already in use`

#### Scenario: Cambiar el backend del frontend
- **WHEN** se compila el frontend con `PUBLIC_API_URL=https://api.example.com`
- **THEN** todas las llamadas HTTP del frontend apuntan a ese origen, y el bundle no contiene la cadena `localhost:8080`

### Requirement: Artefactos generados no versionados
El repositorio MUST NOT versionar dependencias instaladas (`node_modules/`), salidas de build (`dist/`, `target/`) ni archivos subidos por usuarios.

#### Scenario: Clon limpio
- **WHEN** se ejecuta `git ls-files | grep -E 'node_modules/|/upload/'`
- **THEN** el resultado está vacío

