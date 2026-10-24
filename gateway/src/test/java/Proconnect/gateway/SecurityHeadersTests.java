package Proconnect.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec;

/** Headers de seguridad en todas las respuestas (SEC-19) y subida anónima rechazada (SEC-15). */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
		"proconnect.jwt.secret=test-secret-test-secret-test-secret-123",
		"MEDIA_URI=http://localhost:1", "API_URI=http://localhost:1",
		"CORS_ORIGINS=http://localhost:4321"
})
class SecurityHeadersTests {

	@Autowired
	WebTestClient client;

	static void assertSecurityHeaders(ResponseSpec spec) {
		spec.expectHeader().value("Content-Security-Policy", csp -> {
			org.assertj.core.api.Assertions.assertThat(csp).contains("script-src 'self'")
					.contains("frame-ancestors 'none'").doesNotContain("unsafe-eval").doesNotContain("unsafe-inline");
		})
				.expectHeader().valueEquals("X-Content-Type-Options", "nosniff")
				.expectHeader().valueEquals("X-Frame-Options", "DENY")
				.expectHeader().valueEquals("Referrer-Policy", "strict-origin-when-cross-origin")
				.expectHeader().exists("Permissions-Policy");
	}

	@Test
	void headersOnUnauthorizedMediaResponse() {
		ResponseSpec spec = client.get().uri("/upload/uploads/x.png").exchange();
		spec.expectStatus().isUnauthorized();
		assertSecurityHeaders(spec);
		spec.expectHeader().doesNotExist("Strict-Transport-Security");
	}

	@Test
	void headersOnHealthAndNotFound() {
		ResponseSpec health = client.get().uri("/actuator/health").exchange();
		health.expectStatus().isOk();
		assertSecurityHeaders(health);
		ResponseSpec missing = client.get().uri("/no-existe").exchange();
		missing.expectStatus().isNotFound();
		assertSecurityHeaders(missing);
	}

	@Test
	void headersOnProxiedApiErrors() {
		// La API no existe en el test: la respuesta de error del proxy también lleva los headers.
		assertSecurityHeaders(client.get().uri("/api/posts").exchange());
	}

	@Test
	void anonymousUploadIs401() {
		client.post().uri("/upload/subirImagen").contentType(MediaType.MULTIPART_FORM_DATA)
				.bodyValue("--x--").exchange().expectStatus().isUnauthorized();
	}
}
