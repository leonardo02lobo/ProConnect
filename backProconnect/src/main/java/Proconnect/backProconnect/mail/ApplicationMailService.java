package Proconnect.backProconnect.mail;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.retry.RetryException;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Envío del correo de resolución al postulante con reintentos (3 intentos, backoff exponencial). Un
 * fallo definitivo se registra y se descarta: nunca revierte el cambio de estado, que ya está
 * confirmado.
 *
 * <p>Reintentos con el {@link RetryTemplate} del core de Spring Framework 7 (sustituye a
 * spring-retry desde upgrade-spring-boot-4). Solo se reintenta {@link MailException}; cualquier
 * otro error se propaga sin reintentos, como antes.
 */
@Service
public class ApplicationMailService {

    private static final Logger log = LoggerFactory.getLogger(ApplicationMailService.class);

    private final JavaMailSender mailSender;
    private final MailTemplates templates;
    private final String from;
    private final RetryTemplate retry;

    public ApplicationMailService(
            JavaMailSender mailSender,
            MailTemplates templates,
            @Value("${proconnect.mail.from}") String from,
            @Value("${proconnect.mail.retry.max-attempts:3}") int maxAttempts,
            @Value("${proconnect.mail.retry.delay-ms:2000}") long delayMs,
            @Value("${proconnect.mail.retry.multiplier:2}") double multiplier) {
        this.mailSender = mailSender;
        this.templates = templates;
        this.from = from;
        this.retry =
                new RetryTemplate(
                        RetryPolicy.builder()
                                .includes(MailException.class)
                                // maxRetries = reintentos tras el primer intento.
                                .maxRetries(Math.max(maxAttempts, 1) - 1L)
                                .delay(Duration.ofMillis(delayMs))
                                .multiplier(multiplier)
                                .build());
    }

    public void sendResolution(PostulationResolvedEvent event) {
        AtomicInteger attempt = new AtomicInteger();
        try {
            retry.execute(
                    () -> {
                        send(event, attempt.incrementAndGet());
                        return null;
                    });
        } catch (RetryException e) {
            if (e.getLastException() instanceof MailException mailError) {
                giveUp(mailError, event);
            } else if (e.getLastException() instanceof RuntimeException other) {
                throw other;
            } else {
                throw new IllegalStateException(e.getLastException());
            }
        }
    }

    private void send(PostulationResolvedEvent event, int attempt) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(event.applicantEmail());
        message.setSubject(templates.subject(event.jobTitle()));
        message.setText(
                templates.body(
                        event.status(),
                        event.applicantName(),
                        event.jobTitle(),
                        event.companyName()));
        try {
            mailSender.send(message);
            log.info(
                    "Correo de postulación {} ({}) enviado", event.postulationId(), event.status());
        } catch (MailException e) {
            log.warn(
                    "Fallo SMTP enviando el correo de la postulación {} (intento {}): {}",
                    event.postulationId(),
                    attempt,
                    e.getMessage());
            throw e;
        }
    }

    private void giveUp(MailException e, PostulationResolvedEvent event) {
        log.error(
                "Correo de la postulación {} descartado tras agotar los reintentos",
                event.postulationId(),
                e);
    }
}
