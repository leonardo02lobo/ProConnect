## ADDED Requirements

### Requirement: Frameworks en líneas con soporte vigente
Los servicios Java MUST ejecutarse sobre una versión de Spring Boot cuya línea tenga soporte OSS vigente en la fecha de cada release. Spring Cloud MUST usarse solo en versiones GA compatibles con la versión de Boot del módulo, nunca en milestones ni RC.

#### Scenario: Verificar versiones del API core
- **WHEN** se inspecciona `backProconnect/pom.xml`
- **THEN** el parent es `spring-boot-starter-parent` 4.1.x

#### Scenario: Verificar el gateway
- **WHEN** se inspecciona `gateway/pom.xml`
- **THEN** usa Spring Boot 4.x y una versión GA de `spring-cloud-dependencies` declarada compatible con esa versión de Boot

### Requirement: Sin regresiones funcionales
La migración MUST mantener en verde, sin debilitar aserciones ni deshabilitar tests, la suite E2E completa, los tests JUnit de backend y gateway, los tests del servicio de media, los tests de conteo de queries y todos los checks requeridos en `main`.

#### Scenario: Suite completa tras migrar
- **WHEN** se ejecutan `mvn verify` en ambos módulos y la suite E2E contra el stack migrado
- **THEN** todos los tests pasan, con al menos el mismo número de tests que antes de la migración

#### Scenario: Contratos JSON intactos
- **WHEN** se comparan las respuestas JSON del feed, del perfil, de los errores problem+json y de `/api/auth/me` antes y después de la migración
- **THEN** tienen la misma forma: mismos campos, tipos y formato de fechas

### Requirement: Riesgos aceptados revisados
Tras la migración, las supresiones de Dependency-Check y las excepciones de la auditoría que dependían de la línea 3.5 MUST eliminarse o volver a justificarse con fecha de revisión, y el escaneo SCA MUST pasar con el corte vigente (CVSS ≥ 7).

#### Scenario: Supresiones obsoletas
- **WHEN** se ejecuta Dependency-Check sobre los módulos migrados
- **THEN** no quedan supresiones que referencien artefactos de Spring Framework 6.x ni Spring Security 6.x, y no hay hallazgos ≥ 7 sin justificar

### Requirement: Alerta temprana de fin de soporte
El CI MUST incluir un chequeo programado que falle o abra un aviso cuando la línea de Spring Boot usada esté a menos de 60 días de su fin de soporte OSS.

#### Scenario: Fin de soporte próximo
- **WHEN** el chequeo semanal detecta que la línea usada termina su soporte OSS en menos de 60 días
- **THEN** el workflow falla con un mensaje que indica la línea, la fecha de fin y la versión destino recomendada
