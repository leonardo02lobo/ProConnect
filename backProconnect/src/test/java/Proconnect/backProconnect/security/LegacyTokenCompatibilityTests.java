package Proconnect.backProconnect.security;

import static org.assertj.core.api.Assertions.assertThat;

import Proconnect.backProconnect.Entity.User;
import Proconnect.backProconnect.model.Role;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import org.junit.jupiter.api.Test;

/**
 * upgrade-spring-boot-4 (tarea 2.3): las sesiones abiertas antes de la migración siguen siendo
 * válidas después. {@link #BOOT3_TOKEN} lo emitió jjwt 0.12.6 con Jackson 2 (la pila de Spring Boot
 * 3.5) con el secreto de test, exp 2099-12-31; si la serialización JSON o el formato del token
 * cambian, este test falla.
 */
class LegacyTokenCompatibilityTests {

    private static final String SECRET = "test-secret-test-secret-test-secret-123";

    static final String BOOT3_TOKEN =
            "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiI1YjBjNGM2NC0xZjVlLTRmMGEtOWE0My1ib290M3Rva2VuMDAiLCJzdWIiOiI0MjQyIiwicm9sZSI6IkNPTVBBTllfQURNSU4iLCJuYW1lIjoiQW5hIExlZ2FjeSIsImlhdCI6MTc5Mjg4NjQwMCwiZXhwIjo0MTAyNDQ0Nzk5fQ.Rn9a4wyDR2nC9LlK0c8_3JJo5wDOTMq9rftNiVIy_g8";

    private final JwtService jwt = new JwtService(SECRET, Duration.ofHours(1));

    @Test
    void tokenIssuedBeforeTheMigrationStillValidates() {
        AuthPrincipal principal = jwt.parse(BOOT3_TOKEN).orElseThrow();
        assertThat(principal.userId()).isEqualTo(4242L);
        assertThat(principal.role()).isEqualTo(Role.COMPANY_ADMIN);
        assertThat(principal.name()).isEqualTo("Ana Legacy");
    }

    @Test
    void tamperedLegacyTokenIsRejected() {
        String tampered = BOOT3_TOKEN.substring(0, BOOT3_TOKEN.length() - 2) + "AA";
        assertThat(jwt.parse(tampered)).isEmpty();
    }

    @Test
    void newTokensKeepTheSameHeaderAndClaimNames() {
        User user = new User(7L, "Ana", "ana", "ana@example.com", "x", Role.USER);
        String[] parts = jwt.generate(user).split("\\.");
        Base64.Decoder dec = Base64.getUrlDecoder();
        assertThat(new String(dec.decode(parts[0]), StandardCharsets.UTF_8))
                .isEqualTo("{\"alg\":\"HS256\"}");
        String payload = new String(dec.decode(parts[1]), StandardCharsets.UTF_8);
        assertThat(payload)
                .startsWith("{\"jti\":\"")
                .contains("\"sub\":\"7\"", "\"role\":\"USER\"", "\"name\":\"Ana\"")
                .containsPattern("\"iat\":\\d+,\"exp\":\\d+}$");
    }
}
