package Proconnect.backProconnect.mail;

import Proconnect.backProconnect.Entity.PostulationStatus;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * Plantillas de texto plano de los correos de postulación ({@code templates/mail/*.txt}), copiadas
 * literalmente de {@code MensajePositivo}/{@code MensajeNegativo} del backend Express legacy.
 * Placeholders: {@code {nombre}}, {@code {puesto}} y {@code {empresa}}.
 */
@Component
public class MailTemplates {

    private final Map<PostulationStatus, String> templates =
            Map.of(
                    PostulationStatus.ACCEPTED, load("templates/mail/accepted.txt"),
                    PostulationStatus.REJECTED, load("templates/mail/rejected.txt"));

    /** Asunto idéntico al legacy. */
    public String subject(String jobTitle) {
        return "Respuesta hacia el puesto de: " + jobTitle;
    }

    public String body(PostulationStatus status, String name, String jobTitle, String companyName) {
        String template = templates.get(status);
        if (template == null) {
            throw new IllegalArgumentException("Sin plantilla para el estado " + status);
        }
        return template.replace("{nombre}", nullSafe(name))
                .replace("{puesto}", nullSafe(jobTitle))
                .replace("{empresa}", nullSafe(companyName));
    }

    private static String nullSafe(String value) {
        return value == null ? "" : value;
    }

    private static String load(String path) {
        try (InputStream in = new ClassPathResource(path).getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo cargar la plantilla " + path, e);
        }
    }
}
