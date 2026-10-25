package Proconnect.backProconnect.it;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Base vacía: Flyway aplica todas las migraciones y Hibernate valida el esquema (si una entidad no
 * coincidiera, el contexto no arrancaría y este test fallaría). Comprueba además las restricciones
 * de integridad que exige el spec data-model-integrity.
 */
class FlywaySchemaIT extends MySqlIntegrationTest {

    @Autowired JdbcTemplate jdbc;

    @Test
    void allMigrationsAppliedAndSchemaValidated() {
        List<String> failed =
                jdbc.queryForList(
                        "SELECT version FROM flyway_schema_history WHERE success = 0",
                        String.class);
        assertThat(failed).isEmpty();
        Integer applied =
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM flyway_schema_history WHERE version IS NOT NULL",
                        Integer.class);
        assertThat(applied).isGreaterThanOrEqualTo(2);
    }

    @Test
    void foreignKeysAndUniqueConstraintsExist() {
        List<String> fks =
                jdbc.queryForList(
                        "SELECT CONSTRAINT_NAME FROM information_schema.REFERENTIAL_CONSTRAINTS"
                                + " WHERE CONSTRAINT_SCHEMA = DATABASE()",
                        String.class);
        assertThat(fks)
                .contains(
                        "fk_posts_user",
                        "fk_comments_post",
                        "fk_comments_user",
                        "fk_likes_post",
                        "fk_likes_user",
                        "fk_friends_follow",
                        "fk_friends_followed",
                        "fk_messages_sender",
                        "fk_messages_receiver",
                        "fk_company_user",
                        "fk_jobs_company",
                        "fk_postulations_job",
                        "fk_postulations_user",
                        "fk_work_stations_user");
        List<String> uniques =
                jdbc.queryForList(
                        "SELECT DISTINCT INDEX_NAME FROM information_schema.STATISTICS"
                                + " WHERE TABLE_SCHEMA = DATABASE() AND NON_UNIQUE = 0"
                                + " AND INDEX_NAME <> 'PRIMARY'",
                        String.class);
        assertThat(uniques)
                .contains(
                        "uk_likes_user_post",
                        "uk_friends_pair",
                        "uk_postulations_user_job",
                        "uk_user_email",
                        "uk_user_user_name");
        Integer nameUnique =
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM information_schema.STATISTICS WHERE TABLE_SCHEMA ="
                                + " DATABASE() AND TABLE_NAME = 'user' AND COLUMN_NAME = 'name'"
                                + " AND NON_UNIQUE = 0",
                        Integer.class);
        assertThat(nameUnique).isZero();
    }
}
