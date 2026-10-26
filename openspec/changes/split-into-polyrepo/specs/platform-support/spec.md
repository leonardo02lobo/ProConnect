## MODIFIED Requirements

### Requirement: Frameworks en líneas con soporte vigente
Los servicios Java MUST ejecutarse sobre una versión de Spring Boot cuya línea tenga soporte OSS vigente en la fecha de cada release. Spring Cloud MUST usarse solo en versiones GA compatibles con la versión de Boot del módulo, nunca en milestones ni RC.

#### Scenario: Verificar versiones del API core
- **WHEN** se inspecciona `pom.xml` de `proconnect-backend` (`backend/pom.xml` en el principal)
- **THEN** el parent es `spring-boot-starter-parent` 4.1.x

#### Scenario: Verificar el gateway
- **WHEN** se inspecciona `gateway/pom.xml` de `proconnect-services` (`services/gateway/pom.xml` en el principal)
- **THEN** usa Spring Boot 4.x y una versión GA de `spring-cloud-dependencies` declarada compatible con esa versión de Boot
