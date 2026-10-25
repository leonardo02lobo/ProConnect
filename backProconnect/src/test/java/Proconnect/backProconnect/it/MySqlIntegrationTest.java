package Proconnect.backProconnect.it;

import Proconnect.backProconnect.security.SecurityTestSupport;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;

/**
 * Base de los tests de integración ({@code *IT}, maven-failsafe) contra un MySQL 8.4 real con el
 * esquema creado por Flyway y validado por Hibernate ({@code ddl-auto=validate}), igual que en
 * producción. La suite rápida ({@code *Tests}) sigue usando H2.
 *
 * <p>MySQL sale de Testcontainers (un contenedor compartido por todas las clases {@code *IT}). Si
 * Docker no está disponible para el proceso de tests, se puede apuntar a un MySQL ya levantado con
 * {@code IT_DB_URL}/{@code IT_DB_USER}/{@code IT_DB_PASSWORD} (base vacía llamada {@code
 * proconnect}); ver docs/db/migrations.md.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(QueryCounter.Config.class)
public abstract class MySqlIntegrationTest extends SecurityTestSupport {

    private static final String EXTERNAL_URL = System.getenv("IT_DB_URL");
    static final MySQLContainer<?> MYSQL;

    static {
        if (EXTERNAL_URL == null || EXTERNAL_URL.isBlank()) {
            MYSQL =
                    new MySQLContainer<>("mysql:8.4")
                            .withDatabaseName("proconnect")
                            .withUsername("app")
                            .withPassword("app-it-password")
                            .withCommand(
                                    "--character-set-server=utf8mb4",
                                    "--collation-server=utf8mb4_0900_ai_ci");
            MYSQL.start();
        } else {
            MYSQL = null;
        }
    }

    static String jdbcUrl() {
        return MYSQL != null ? MYSQL.getJdbcUrl() : EXTERNAL_URL;
    }

    static String dbUser() {
        return MYSQL != null
                ? MYSQL.getUsername()
                : System.getenv().getOrDefault("IT_DB_USER", "root");
    }

    static String dbPassword() {
        return MYSQL != null
                ? MYSQL.getPassword()
                : System.getenv().getOrDefault("IT_DB_PASSWORD", "");
    }

    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MySqlIntegrationTest::jdbcUrl);
        registry.add("spring.datasource.username", MySqlIntegrationTest::dbUser);
        registry.add("spring.datasource.password", MySqlIntegrationTest::dbPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.MySQLDialect");
    }
}
