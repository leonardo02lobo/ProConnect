package Proconnect.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

/** Con el perfil prod el gateway envía HSTS (max-age >= 180 días). */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "proconnect.jwt.secret=test-secret-test-secret-test-secret-123",
            "MEDIA_URI=http://localhost:1"
        })
@ActiveProfiles("prod")
@AutoConfigureWebTestClient
class HstsProdProfileTests {

    @Autowired WebTestClient client;

    @Test
    void hstsInProd() {
        client.get()
                .uri("/actuator/health")
                .exchange()
                .expectHeader()
                .valueEquals("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
    }
}
