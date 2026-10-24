package Proconnect.backProconnect.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/** Rate limiting y bloqueo por fuerza bruta (SEC-11) con los límites reales: 10/min por IP, 5 fallos, 15 min. */
@SpringBootTest(properties = { "proconnect.ratelimit.auth-per-minute=10", "proconnect.ratelimit.max-failures=5",
        "proconnect.ratelimit.lockout=PT15M" })
@AutoConfigureMockMvc
class RateLimitTests extends SecurityTestSupport {

    private MockHttpServletRequestBuilder login(String email, String password, String clientIp) {
        return post("/api/auth/login").header("X-Forwarded-For", clientIp).contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}");
    }

    @Test
    void accountIsLockedAfterFiveFailures() throws Exception {
        createUser("ana", "USER");
        for (int i = 0; i < 5; i++) {
            // IPs distintas: el bloqueo es por cuenta, no por IP.
            mvc.perform(login("ana@example.com", "wrong-password", "203.0.113." + i))
                    .andExpect(status().isUnauthorized());
        }
        mvc.perform(login("ana@example.com", PASSWORD, "203.0.113.50")).andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"));
        mvc.perform(login("ANA@example.com", PASSWORD, "203.0.113.51")).andExpect(status().isTooManyRequests());
    }

    @Test
    void successResetsFailureCounter() throws Exception {
        createUser("bob", "USER");
        for (int i = 0; i < 4; i++) {
            mvc.perform(login("bob@example.com", "wrong-password", "198.51.100.1")).andExpect(status().isUnauthorized());
        }
        mvc.perform(login("bob@example.com", PASSWORD, "198.51.100.1")).andExpect(status().isOk());
        mvc.perform(login("bob@example.com", "wrong-password", "198.51.100.1")).andExpect(status().isUnauthorized());
        mvc.perform(login("bob@example.com", PASSWORD, "198.51.100.1")).andExpect(status().isOk());
    }

    @Test
    void ipIsLimitedToTenRequestsPerMinute() throws Exception {
        for (int i = 0; i < 10; i++) {
            mvc.perform(login("ghost" + i + "@example.com", "x", "192.0.2.7")).andExpect(status().isUnauthorized());
        }
        String retry = mvc.perform(login("ghost@example.com", "x", "192.0.2.7"))
                .andExpect(status().isTooManyRequests()).andReturn().getResponse().getHeader("Retry-After");
        assertThat(Long.parseLong(retry)).isBetween(1L, 60L);
        // Registro comparte el límite por IP.
        mvc.perform(post("/api/auth/register").header("X-Forwarded-For", "192.0.2.7")
                .contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isTooManyRequests());
        // Otra IP no está afectada.
        mvc.perform(login("ghost@example.com", "x", "192.0.2.8")).andExpect(status().isUnauthorized());
    }

    @Test
    void forwardedForIsIgnoredFromUntrustedPeers() throws Exception {
        for (int i = 0; i < 10; i++) {
            final int n = i;
            // El cliente (no proxy de confianza) intenta rotar IPs falsificando X-Forwarded-For.
            mvc.perform(login("spoof" + n + "@example.com", "x", "10.9.9." + n).with(r -> {
                r.setRemoteAddr("198.18.0.1");
                return r;
            })).andExpect(status().isUnauthorized());
        }
        mvc.perform(login("spoof99@example.com", "x", "10.9.9.99").with(r -> {
            r.setRemoteAddr("198.18.0.1");
            return r;
        })).andExpect(status().isTooManyRequests());
    }
}
