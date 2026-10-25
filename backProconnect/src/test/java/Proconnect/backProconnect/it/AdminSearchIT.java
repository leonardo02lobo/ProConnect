package Proconnect.backProconnect.it;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Búsqueda de {@code GET /api/admin/users} contra MySQL real: el {@code ESCAPE '\'} de la JPQL y el
 * escapado de comodines de {@code RoleService} deben comportarse igual que en H2 (en MySQL la barra
 * invertida también es escape dentro de los literales de cadena).
 */
class AdminSearchIT extends MySqlIntegrationTest {

    private Cookie root;
    private Long percentId;

    @BeforeEach
    void dataset() throws Exception {
        createUser("root", "ADMIN");
        createUser("ana", "USER");
        percentId = createUser("pct%x", "USER");
        createUser("und_x", "USER");
        createUser("undyx", "USER");
        root = login("root");
    }

    @Test
    void searchMatchesNameUserNameAndEmailCaseInsensitive() throws Exception {
        mvc.perform(get("/api/admin/users").param("q", "ANA").cookie(root))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].email").value("ana@example.com"))
                .andExpect(jsonPath("$.totalElements").value(1));
        mvc.perform(get("/api/admin/users").cookie(root))
                .andExpect(jsonPath("$.totalElements").value(5));
    }

    @Test
    void likeWildcardsAreLiterals() throws Exception {
        mvc.perform(get("/api/admin/users").param("q", "%").cookie(root))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(percentId));
        mvc.perform(get("/api/admin/users").param("q", "und_").cookie(root))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].name").value("und_x"));
        mvc.perform(get("/api/admin/users").param("q", "\\").cookie(root))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }
}
