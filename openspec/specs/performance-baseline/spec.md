# performance-baseline Specification

## Purpose
Línea base de rendimiento reproducible (k6) para comparar cambios.
## Requirements
### Requirement: Dataset de referencia reproducible
El proyecto MUST incluir un generador de datos sintéticos determinista (con semilla fija) que cree el dataset de referencia: 1.000 usuarios, 10.000 posts, 50.000 likes, 30.000 comentarios y 5.000 relaciones de amistad.

#### Scenario: Generar el dataset dos veces
- **WHEN** se ejecuta el generador dos veces con la misma semilla sobre bases vacías
- **THEN** ambas bases contienen exactamente los mismos registros

### Requirement: Medición de queries y latencia
El proyecto MUST proveer un procedimiento automatizado que, sobre el dataset de referencia, mida para los endpoints calientes (feed, perfil, comentarios de un post, like) el número de sentencias SQL por petición y la latencia p50/p95, y que guarde los resultados en un archivo versionado.

#### Scenario: Ejecutar la medición
- **WHEN** se ejecuta el script de benchmark
- **THEN** se genera `docs/perf/<fecha>-<commit>.md` con una tabla endpoint × (queries, p50, p95)

### Requirement: Umbrales de aceptación
Tras este change, con el dataset de referencia y en el entorno de CI, el feed y el perfil MUST cumplir: ≤ 4 sentencias SQL por petición y p95 ≤ 150 ms por petición individual en caliente.

#### Scenario: Comparación antes y después
- **WHEN** se comparan la medición base (antes del change) y la final
- **THEN** el informe muestra la reducción de queries y de latencia, y los valores finales cumplen los umbrales

### Requirement: Detección de regresiones N+1
La suite de tests de backend MUST incluir tests que fallen si el número de sentencias SQL del feed o del perfil supera el umbral.

#### Scenario: Se introduce un acceso lazy en un bucle
- **WHEN** un cambio provoca que el feed cargue el autor de cada comentario con una query individual
- **THEN** el test de conteo de queries falla en CI

