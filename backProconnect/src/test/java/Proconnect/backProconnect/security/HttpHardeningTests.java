package Proconnect.backProconnect.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import jakarta.servlet.http.Cookie;

/** CSRF por Origin/Referer (SEC-10), cookie de sesión (SEC-14) y revocación en logout (SEC-12). */
@SpringBootTest
@AutoConfigureMockMvc
class HttpHardeningTests extends SecurityTestSupport {

    private static final String POST_BODY = "{\"title\":\"t\",\"content\":\"c\"}";

    @Test
    void mutationFromForeignOriginIsRejected() throws Exception {
        createUser("ana", "USER");
        Cookie ana = login("ana");
        mvc.perform(post("/api/posts").cookie(ana).header("Origin", "https://evil.example")
                .contentType(MediaType.APPLICATION_JSON).content(POST_BODY)).andExpect(status().isForbidden());
        mvc.perform(post("/api/posts").cookie(ana).header("Referer", "https://evil.example/form.html")
                .contentType(MediaType.APPLICATION_JSON).content(POST_BODY)).andExpect(status().isForbidden());
        mvc.perform(post("/api/posts").cookie(ana).header("Origin", "null")
                .contentType(MediaType.APPLICATION_JSON).content(POST_BODY)).andExpect(status().isForbidden());
        assertThat(posts.count()).isZero();

        mvc.perform(post("/api/posts").cookie(ana).header("Origin", "http://localhost:4321")
                .contentType(MediaType.APPLICATION_JSON).content(POST_BODY)).andExpect(status().isCreated());
        mvc.perform(post("/api/posts").cookie(ana).header("Referer", "http://localhost:4321/feed")
                .contentType(MediaType.APPLICATION_JSON).content(POST_BODY)).andExpect(status().isCreated());
        // Sin Origin ni Referer (cliente no navegador / SSR): se permite.
        mvc.perform(post("/api/posts").cookie(ana)
                .contentType(MediaType.APPLICATION_JSON).content(POST_BODY)).andExpect(status().isCreated());
        assertThat(posts.count()).isEqualTo(3);
    }

    @Test
    void loginFromForeignOriginIsRejected() throws Exception {
        createUser("ana", "USER");
        mvc.perform(post("/api/auth/login").header("Origin", "https://evil.example")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"ana@example.com\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void sessionCookieIsHardened() {
        String prod = new SessionCookieFactory("pc_session", true).session("tok", Duration.ofHours(1)).toString();
        assertThat(prod).contains("HttpOnly").contains("Secure").contains("SameSite=Lax").contains("Path=/");
        String dev = new SessionCookieFactory("pc_session", false).session("tok", Duration.ofHours(1)).toString();
        assertThat(dev).contains("HttpOnly").doesNotContain("Secure");
    }

    @Test
    void logoutRevokesTheToken() throws Exception {
        createUser("ana", "USER");
        Cookie ana = login("ana");
        mvc.perform(get("/api/auth/me").cookie(ana)).andExpect(status().isOk());
        mvc.perform(post("/api/auth/logout").cookie(ana)).andExpect(status().isNoContent());
        // El mismo JWT reutilizado tras el logout ya no sirve.
        mvc.perform(get("/api/auth/me").cookie(ana)).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/posts").cookie(ana)).andExpect(status().isUnauthorized());
        // Un login nuevo emite otro token válido.
        mvc.perform(get("/api/auth/me").cookie(login("ana"))).andExpect(status().isOk());
    }
}
