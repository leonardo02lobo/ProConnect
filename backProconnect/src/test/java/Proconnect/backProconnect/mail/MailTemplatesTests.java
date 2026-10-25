package Proconnect.backProconnect.mail;

import static org.assertj.core.api.Assertions.assertThat;

import Proconnect.backProconnect.Entity.PostulationStatus;
import org.junit.jupiter.api.Test;

/**
 * Plantillas copiadas del legacy (MensajePositivo/MensajeNegativo) con los placeholders
 * sustituidos.
 */
class MailTemplatesTests {

    private final MailTemplates templates = new MailTemplates();

    @Test
    void subjectMatchesLegacy() {
        assertThat(templates.subject("Backend Dev"))
                .isEqualTo("Respuesta hacia el puesto de: Backend Dev");
    }

    @Test
    void acceptedBodyIsThePositiveLegacyMessage() {
        String body = templates.body(PostulationStatus.ACCEPTED, "Ana", "Backend Dev", "Acme");
        assertThat(body)
                .startsWith("¡Felicidades! Has sido seleccionado para el puesto")
                .contains("Hola Ana,")
                .contains("has sido seleccionado para el puesto de Backend Dev en Acme!")
                .contains("¡Bienvenido a bordo!")
                .endsWith("Atentamente,\nAcme\n")
                .doesNotContain("{nombre}")
                .doesNotContain("{puesto}")
                .doesNotContain("{empresa}");
    }

    @Test
    void rejectedBodyIsTheNegativeLegacyMessage() {
        String body = templates.body(PostulationStatus.REJECTED, "Ana", "Backend Dev", "Acme");
        assertThat(body)
                .startsWith("Resultado de tu postulación al puesto de Backend Dev")
                .contains("Hola Ana")
                .contains("formar parte de Acme")
                .contains("hemos decidido continuar con otros candidatos")
                .doesNotContain("Felicidades")
                .doesNotContain("{");
    }
}
