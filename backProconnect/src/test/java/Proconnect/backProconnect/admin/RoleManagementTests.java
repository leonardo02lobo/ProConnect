package Proconnect.backProconnect.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import Proconnect.backProconnect.Entity.RoleChange;
import Proconnect.backProconnect.exception.ConflictException;
import Proconnect.backProconnect.exception.ForbiddenException;
import Proconnect.backProconnect.model.Role;
import Proconnect.backProconnect.repository.RoleChangeRepository;
import Proconnect.backProconnect.security.AuthPrincipal;
import Proconnect.backProconnect.security.RoleResolver;
import Proconnect.backProconnect.security.SecurityTestSupport;
import Proconnect.backProconnect.service.RoleService;
import jakarta.servlet.http.Cookie;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.ResultActions;

/** add-role-management: asignación de roles por un ADMIN, salvaguardas, auditoría y efecto. */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "proconnect.security.role-cache-ttl=PT30S")
class RoleManagementTests extends SecurityTestSupport {

    private static final String COMPANY =
            "{\"name\":\"ACME\",\"description\":\"d\",\"location\":\"l\"}";

    @Autowired RoleChangeRepository roleChanges;
    @Autowired RoleService roleService;
    @Autowired RoleResolver roleResolver;

    Long anaId;
    Long carlaId;
    Long rootId;
    Long otherAdminId;
    Cookie ana;
    Cookie carla;
    Cookie root;
    Cookie otherAdmin;

    @BeforeEach
    void users() throws Exception {
        // Contexto propio con la cache real; otro contexto pudo recrear el schema (ids
        // reutilizados).
        roleResolver.invalidateAll();
        anaId = createUser("ana", "USER");
        carlaId = createUser("carla", "COMPANY_ADMIN");
        rootId = createUser("root", "ADMIN");
        otherAdminId = createUser("rita", "ADMIN");
        ana = login("ana");
        carla = login("carla");
        root = login("root");
        otherAdmin = login("rita");
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private ResultActions changeRole(Cookie session, Long userId, String body) throws Exception {
        return mvc.perform(
                put("/api/admin/users/" + userId + "/role")
                        .cookie(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body));
    }

    private String roleOf(Long id) {
        return users.findById(id).orElseThrow().getRole().name();
    }

    private ResultActions createCompany(Cookie session) throws Exception {
        return mvc.perform(
                post("/api/companies")
                        .cookie(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(COMPANY));
    }

    @Test
    void adminPromotesUserToCompanyAdminWithAudit() throws Exception {
        // Ana tiene sesión abierta antes de la promoción: no puede crear empresas.
        createCompany(ana).andExpect(status().isForbidden());

        changeRole(root, anaId, "{\"role\":\"COMPANY_ADMIN\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(anaId))
                .andExpect(jsonPath("$.role").value("COMPANY_ADMIN"))
                .andExpect(jsonPath("$.email").value("ana@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist());
        assertThat(roleOf(anaId)).isEqualTo("COMPANY_ADMIN");

        List<RoleChange> audit = roleChanges.findByTargetUserIdOrderByIdAsc(anaId);
        assertThat(audit).hasSize(1);
        RoleChange change = audit.get(0);
        assertThat(change.getActorId()).isEqualTo(rootId);
        assertThat(change.getFromRole()).isEqualTo("USER");
        assertThat(change.getToRole()).isEqualTo("COMPANY_ADMIN");
        assertThat(change.getChangedAt()).isNotNull();

        // Efecto inmediato con la misma cookie (el JWT sigue diciendo USER).
        createCompany(ana).andExpect(status().isCreated());
    }

    @Test
    void nonAdminsGet403AndRoleDoesNotChange() throws Exception {
        for (Cookie session : List.of(ana, carla)) {
            changeRole(session, anaId, "{\"role\":\"ADMIN\"}")
                    .andExpect(status().isForbidden())
                    .andExpect(content().contentTypeCompatibleWith("application/problem+json"));
            mvc.perform(get("/api/admin/users").cookie(session)).andExpect(status().isForbidden());
        }
        assertThat(roleOf(anaId)).isEqualTo("USER");
        assertThat(roleChanges.findByTargetUserIdOrderByIdAsc(anaId)).isEmpty();
    }

    @Test
    void invalidRoleIs400ProblemJson() throws Exception {
        for (String body :
                List.of(
                        "{\"role\":\"SUPERUSER\"}",
                        "{\"role\":\"admin\"}",
                        "{\"role\":\"\"}",
                        "{}")) {
            changeRole(root, anaId, body)
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.errors[0].field").value("role"));
        }
        assertThat(roleOf(anaId)).isEqualTo("USER");
    }

    @Test
    void unknownUserIs404() throws Exception {
        changeRole(root, 999_999L, "{\"role\":\"USER\"}").andExpect(status().isNotFound());
    }

    @Test
    void adminCannotDemoteThemselves() throws Exception {
        changeRole(root, rootId, "{\"role\":\"USER\"}")
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"));
        assertThat(roleOf(rootId)).isEqualTo("ADMIN");
        assertThat(roleChanges.findByTargetUserIdOrderByIdAsc(rootId)).isEmpty();
    }

    @Test
    void sameRoleIsNoOpWithoutAudit() throws Exception {
        changeRole(root, anaId, "{\"role\":\"USER\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("USER"));
        changeRole(root, rootId, "{\"role\":\"ADMIN\"}").andExpect(status().isOk());
        assertThat(roleChanges.findByTargetUserIdOrderByIdAsc(anaId)).isEmpty();
        assertThat(roleChanges.findByTargetUserIdOrderByIdAsc(rootId)).isEmpty();
    }

    @Test
    void lastAdminCannotBeDemotedByAConcurrentlyDemotedAdmin() throws Exception {
        // root degrada a rita; rita, con su rol ADMIN aún en cache o en vuelo, intenta degradar a
        // root.
        changeRole(root, otherAdminId, "{\"role\":\"USER\"}").andExpect(status().isOk());
        actAs(otherAdminId, Role.ADMIN);

        assertThatThrownBy(() -> roleService.changeRole(rootId, "USER"))
                .isInstanceOf(ConflictException.class);
        assertThat(roleOf(rootId)).isEqualTo("ADMIN");
        assertThat(users.countByRole(Role.ADMIN)).isEqualTo(1);
    }

    @Test
    void staleAdminCannotChangeOtherRoles() throws Exception {
        changeRole(root, otherAdminId, "{\"role\":\"USER\"}").andExpect(status().isOk());
        actAs(otherAdminId, Role.ADMIN);

        assertThatThrownBy(() -> roleService.changeRole(anaId, "COMPANY_ADMIN"))
                .isInstanceOf(ForbiddenException.class);
        assertThat(roleOf(anaId)).isEqualTo("USER");
    }

    @Test
    void demotionAppliesToOpenSessionImmediately() throws Exception {
        // Carla (COMPANY_ADMIN) crea una empresa: su rol queda en la cache de RoleResolver.
        createCompany(carla).andExpect(status().isCreated());

        changeRole(root, carlaId, "{\"role\":\"USER\"}").andExpect(status().isOk());

        // Misma cookie, sin re-login: 403.
        createCompany(carla).andExpect(status().isForbidden());
        mvc.perform(get("/api/auth/me").cookie(carla))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void demotedAdminLosesAdminRoutesImmediately() throws Exception {
        mvc.perform(get("/api/admin/users").cookie(otherAdmin)).andExpect(status().isOk());
        changeRole(root, otherAdminId, "{\"role\":\"USER\"}").andExpect(status().isOk());
        mvc.perform(get("/api/admin/users").cookie(otherAdmin)).andExpect(status().isForbidden());
    }

    @Test
    void deletedUserSessionIsRejected() throws Exception {
        mvc.perform(get("/api/auth/me").cookie(ana)).andExpect(status().isOk());
        users.deleteById(anaId);
        // La entrada en cache caduca o se invalida; forzamos la consulta a la BD.
        roleResolver.invalidate(anaId);
        mvc.perform(get("/api/auth/me").cookie(ana)).andExpect(status().isUnauthorized());
    }

    @Test
    void adminListsAndSearchesUsersWithPagination() throws Exception {
        mvc.perform(get("/api/admin/users").param("q", "ANA").cookie(root))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(anaId))
                .andExpect(jsonPath("$.content[0].userName").value("ana_u"))
                .andExpect(jsonPath("$.content[0].email").value("ana@example.com"))
                .andExpect(jsonPath("$.content[0].role").value("USER"))
                .andExpect(jsonPath("$.content[0].password").doesNotExist())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.page").value(0));

        // Por email.
        mvc.perform(get("/api/admin/users").param("q", "carla@example").cookie(root))
                .andExpect(jsonPath("$.content[0].id").value(carlaId))
                .andExpect(jsonPath("$.content[0].role").value("COMPANY_ADMIN"));

        // Comodines de LIKE escapados.
        mvc.perform(get("/api/admin/users").param("q", "%").cookie(root))
                .andExpect(jsonPath("$.totalElements").value(0));

        mvc.perform(get("/api/admin/users").param("size", "2").param("page", "1").cookie(root))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.totalElements").value(4))
                .andExpect(jsonPath("$.totalPages").value(2));

        mvc.perform(get("/api/admin/users").param("size", "1000").cookie(root))
                .andExpect(jsonPath("$.size").value(RoleService.MAX_PAGE_SIZE));
    }

    private static void actAs(Long userId, Role role) {
        AuthPrincipal principal = new AuthPrincipal(userId, role, "test");
        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                principal,
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))));
    }
}
