package Proconnect.gateway;

import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "proconnect.jwt.secret=test-secret-test-secret-test-secret-123",
            "MEDIA_URI=http://localhost:1",
            "CORS_ORIGINS=http://localhost:4321"
        })
class GatewayApplicationTests {

    private static final String SECRET = "test-secret-test-secret-test-secret-123";

    @Autowired WebTestClient client;

    @Test
    void uploadWithoutCookieIs401() {
        client.get().uri("/upload/uploads/x.png").exchange().expectStatus().isUnauthorized();
    }

    @Test
    void uploadWithForgedTokenIs401() {
        String forged =
                Jwts.builder()
                        .subject("1")
                        .signWith(
                                Keys.hmacShaKeyFor(
                                        "another-secret-another-secret-another"
                                                .getBytes(StandardCharsets.UTF_8)))
                        .compact();
        client.get()
                .uri("/upload/uploads/x.png")
                .cookie("pc_session", forged)
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    void uploadWithExpiredTokenIs401() {
        String expired =
                Jwts.builder()
                        .subject("1")
                        .expiration(new Date(System.currentTimeMillis() - 60_000))
                        .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                        .compact();
        client.get()
                .uri("/upload/uploads/x.png")
                .cookie("pc_session", expired)
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    void uploadWithValidTokenPassesFilter() {
        String token =
                Jwts.builder()
                        .subject("1")
                        .expiration(new Date(System.currentTimeMillis() + 60_000))
                        .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                        .compact();
        // El backend de media no existe en el test: pasar el filtro implica un error de proxy, no
        // un 401.
        client.get()
                .uri("/upload/uploads/x.png")
                .cookie("pc_session", token)
                .exchange()
                .expectStatus()
                .value(s -> org.assertj.core.api.Assertions.assertThat(s).isNotEqualTo(401));
    }

    @Test
    void preflightFromAllowedOriginGetsExactOrigin() {
        client.options()
                .uri("/api/posts")
                .header(HttpHeaders.ORIGIN, "http://localhost:4321")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                .exchange()
                .expectHeader()
                .valueEquals(ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:4321")
                .expectHeader()
                .valueEquals(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
    }

    @Test
    void preflightFromUnknownOriginIsRejected() {
        client.options()
                .uri("/api/posts")
                .header(HttpHeaders.ORIGIN, "http://evil.example")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                .exchange()
                .expectHeader()
                .doesNotExist(ACCESS_CONTROL_ALLOW_ORIGIN);
    }
}
