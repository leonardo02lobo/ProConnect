package Proconnect.backProconnect.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * Migración de una base EXISTENTE creada por Hibernate ({@code ddl-auto=update}, sin Flyway) con
 * datos sucios: Flyway la marca como V1 (baseline-on-migrate) y aplica el resto. Comprueba la
 * auditoría, la limpieza de huérfanos y duplicados y que los datos válidos se conservan.
 *
 * <p>Usa una base propia ({@code legacy}) en el mismo MySQL de Testcontainers, con root.
 */
class LegacyDatabaseMigrationIT {

    private static JdbcTemplate jdbc;
    private static DriverManagerDataSource legacy;

    @BeforeAll
    static void createLegacyDatabase() throws Exception {
        assumeTrue(MySqlIntegrationTest.MYSQL != null, "solo con Testcontainers (necesita root)");
        String rootUrl = MySqlIntegrationTest.MYSQL.getJdbcUrl();
        String password = MySqlIntegrationTest.MYSQL.getPassword();
        try (Connection c = DriverManager.getConnection(rootUrl, "root", password);
                Statement st = c.createStatement()) {
            st.execute("DROP DATABASE IF EXISTS legacy");
            st.execute("CREATE DATABASE legacy");
        }
        legacy =
                new DriverManagerDataSource(
                        rootUrl.replace("/proconnect", "/legacy"), "root", password);
        jdbc = new JdbcTemplate(legacy);
        // Esquema tal como lo dejaba Hibernate (incluido el UNIQUE con nombre aleatorio en
        // user.name que tenían las bases anteriores a cleanup-codebase).
        for (String ddl :
                List.of(
                        "CREATE TABLE `user` (id bigint NOT NULL AUTO_INCREMENT, email varchar(255)"
                                + " NOT NULL, name varchar(255) NOT NULL, password varchar(100) NOT"
                                + " NULL, role varchar(255) NOT NULL, user_name varchar(255) NOT"
                                + " NULL, PRIMARY KEY (id), UNIQUE KEY UKob8kqyqqgmefl0aco34akdtpe"
                                + " (email), UNIQUE KEY UKgj2fy3dcix7ph7k8684gka40c (name), UNIQUE"
                                + " KEY UKlqjrcobrh9jc8wpcar64q1bfh (user_name))",
                        "CREATE TABLE profile_photos (id bigint NOT NULL AUTO_INCREMENT, photo_url"
                                + " varchar(255), user_id bigint NOT NULL, wall_photo_url"
                                + " varchar(255), PRIMARY KEY (id))",
                        "CREATE TABLE posts (id bigint NOT NULL AUTO_INCREMENT, content"
                                + " varchar(255) NOT NULL, date datetime(6), image_url varchar(255),"
                                + " title varchar(255) NOT NULL, user_id bigint NOT NULL, PRIMARY"
                                + " KEY (id))",
                        "CREATE TABLE comments (id bigint NOT NULL AUTO_INCREMENT, content"
                                + " varchar(255) NOT NULL, date datetime(6), post_id bigint NOT"
                                + " NULL, user_id bigint NOT NULL, PRIMARY KEY (id))",
                        "CREATE TABLE likes (id bigint NOT NULL AUTO_INCREMENT, date datetime(6),"
                                + " post_id bigint NOT NULL, user_id bigint NOT NULL, PRIMARY KEY"
                                + " (id))",
                        "CREATE TABLE friends (id bigint NOT NULL AUTO_INCREMENT, status"
                                + " varchar(255) NOT NULL, user_id_be_followed bigint NOT NULL,"
                                + " user_id_follow bigint NOT NULL, PRIMARY KEY (id))",
                        "CREATE TABLE messages (id bigint NOT NULL AUTO_INCREMENT, content"
                                + " varchar(255) NOT NULL, receiver_id bigint NOT NULL, sender_id"
                                + " bigint NOT NULL, timestamp datetime(6), PRIMARY KEY (id))",
                        "CREATE TABLE company (id bigint NOT NULL AUTO_INCREMENT, description"
                                + " varchar(255) NOT NULL, location varchar(255) NOT NULL, name"
                                + " varchar(255) NOT NULL, user_id bigint NOT NULL, PRIMARY KEY"
                                + " (id))",
                        "CREATE TABLE jobs (id bigint NOT NULL AUTO_INCREMENT, company_id bigint"
                                + " NOT NULL, date datetime(6), description varchar(255) NOT NULL,"
                                + " title varchar(255) NOT NULL, work_station varchar(255) NOT NULL,"
                                + " PRIMARY KEY (id))",
                        "CREATE TABLE postulations (id bigint NOT NULL AUTO_INCREMENT, date"
                                + " datetime(6), job_id bigint NOT NULL, status varchar(16) NOT NULL"
                                + " DEFAULT 'PENDING', user_id bigint NOT NULL, PRIMARY KEY (id))",
                        "CREATE TABLE work_stations (id bigint NOT NULL AUTO_INCREMENT, user_id"
                                + " bigint NOT NULL, work_station varchar(255) NOT NULL, PRIMARY KEY"
                                + " (id))")) {
            jdbc.execute(ddl);
        }
        // Datos: usuarios 1 y 2 válidos; el 99 no existe (huérfanos).
        jdbc.execute(
                "INSERT INTO `user` VALUES (1,'a@x.test','Ana','$2a$12$x','user','ana'),"
                        + " (2,'b@x.test','Bob','$2a$12$x',' COMPANY_ADMIN ','bob')");
        jdbc.execute(
                "INSERT INTO profile_photos VALUES (1,'old.png',1,NULL),(2,'new.png',1,'w.png'),"
                        + " (3,'ghost.png',99,NULL)");
        jdbc.execute(
                "INSERT INTO posts VALUES (1,'c','2025-01-01 10:00:00','p.png','t1',1),"
                        + " (2,'c','2025-01-02 10:00:00',NULL,'t2',2),"
                        + " (3,'c','2025-01-03 10:00:00',NULL,'huerfano',99)");
        jdbc.execute(
                "INSERT INTO likes VALUES (1,'2025-01-05',1,2),(2,'2025-01-06',1,2),"
                        + " (3,'2025-01-07',2,1),(4,'2025-01-07',3,1),(5,'2025-01-07',1,99)");
        jdbc.execute(
                "INSERT INTO comments VALUES (1,'hola','2025-01-05',1,2),(2,'x','2025-01-05',3,1),"
                        + " (3,'y','2025-01-05',1,99),(4,'z','2025-01-06',2,1)");
        jdbc.execute(
                "INSERT INTO friends VALUES (1,'accepted',2,1),(2,'PENDING',2,1),(3,'PENDING',99,1),"
                        + " (4,'raro',1,2)");
        jdbc.execute(
                "INSERT INTO messages VALUES (1,'m',2,1,'2025-01-01'),(2,'m',99,1,'2025-01-01')");
        jdbc.execute("INSERT INTO company VALUES (1,'d','l','Acme',2),(2,'d','l','Fantasma',99)");
        jdbc.execute(
                "INSERT INTO jobs VALUES (1,1,'2025-01-01','d','Dev','w'),(2,2,'2025-01-01','d','X','w')");
        jdbc.execute(
                "INSERT INTO postulations VALUES (1,'2025-01-02',1,'PENDING',1),"
                        + " (2,'2025-01-03',1,'PENDING',1),(3,'2025-01-03',2,'PENDING',1)");
        jdbc.execute("INSERT INTO work_stations VALUES (1,1,'Backend'),(2,99,'Nada')");

        Flyway.configure()
                .dataSource(legacy)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .baselineVersion("1")
                .load()
                .migrate();
    }

    private static long count(String sql) {
        Long n = jdbc.queryForObject(sql, Long.class);
        return n == null ? 0 : n;
    }

    @Test
    void baselinesAsV1AndAppliesTheRest() {
        List<Map<String, Object>> history =
                jdbc.queryForList(
                        "SELECT version, type, success FROM flyway_schema_history ORDER BY"
                                + " installed_rank");
        assertThat(history.get(0)).containsEntry("version", "1").containsEntry("type", "BASELINE");
        assertThat(history).allSatisfy(row -> assertThat(row.get("success")).isIn(true, 1));
        assertThat(history.size()).isGreaterThan(1);
    }

    @Test
    void auditsBeforeCleaning() {
        Map<String, Long> audit =
                jdbc.query(
                        "SELECT check_name, affected_rows FROM migration_audit WHERE version ="
                                + " 'V2'",
                        rs -> {
                            Map<String, Long> m = new java.util.HashMap<>();
                            while (rs.next()) {
                                m.put(rs.getString(1), rs.getLong(2));
                            }
                            return m;
                        });
        assertThat(audit)
                .containsEntry("orphan_posts", 1L)
                .containsEntry("orphan_profile_photos", 1L)
                .containsEntry("orphan_company", 1L)
                .containsEntry("orphan_jobs", 1L)
                .containsEntry("orphan_work_stations", 1L)
                .containsEntry("duplicate_likes", 1L)
                .containsEntry("duplicate_postulations", 1L)
                .containsEntry("duplicate_profile_photos", 1L)
                .containsEntry("duplicate_friends", 1L);
    }

    @Test
    void removesOrphansAndDuplicatesKeepingValidData() {
        assertThat(count("SELECT COUNT(*) FROM posts")).isEqualTo(2);
        // like duplicado (1,2): se conserva el más antiguo (id 1); fuera los de post/usuario
        // inexistente.
        assertThat(jdbc.queryForList("SELECT id FROM likes ORDER BY id", Long.class))
                .containsExactly(1L, 3L);
        assertThat(jdbc.queryForList("SELECT id FROM comments ORDER BY id", Long.class))
                .containsExactly(1L, 4L);
        assertThat(jdbc.queryForList("SELECT id FROM friends ORDER BY id", Long.class))
                .containsExactly(1L, 4L);
        assertThat(count("SELECT COUNT(*) FROM messages")).isEqualTo(1);
        assertThat(jdbc.queryForList("SELECT id FROM company", Long.class)).containsExactly(1L);
        assertThat(jdbc.queryForList("SELECT id FROM postulations ORDER BY id", Long.class))
                .containsExactly(1L);
        assertThat(count("SELECT COUNT(*) FROM work_stations")).isEqualTo(1);
    }

    @Test
    void dropsUniqueOnUserName() {
        assertThat(
                        count(
                                "SELECT COUNT(*) FROM information_schema.STATISTICS WHERE"
                                        + " TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user' AND"
                                        + " COLUMN_NAME = 'name' AND NON_UNIQUE = 0"))
                .isZero();
    }

    /** V2.1: enums normalizados (mayúsculas, desconocido → valor seguro) y fechas renombradas. */
    @Test
    void normalizesEnumsAndRenamesDateColumns() {
        assertThat(jdbc.queryForObject("SELECT role FROM `user` WHERE id = 2", String.class))
                .isEqualTo("COMPANY_ADMIN");
        assertThat(jdbc.queryForObject("SELECT role FROM `user` WHERE id = 1", String.class))
                .isEqualTo("USER");
        assertThat(jdbc.queryForList("SELECT status FROM friends ORDER BY id", String.class))
                .containsExactly("ACCEPTED", "PENDING");
        assertThat(
                        jdbc.queryForObject(
                                "SELECT DATE_FORMAT(created_at, '%Y-%m-%d %H:%i') FROM posts WHERE"
                                        + " id = 1",
                                String.class))
                .isEqualTo("2025-01-01 10:00");
        assertThat(count("SELECT COUNT(*) FROM messages WHERE sent_at IS NOT NULL")).isEqualTo(1);
    }
}
