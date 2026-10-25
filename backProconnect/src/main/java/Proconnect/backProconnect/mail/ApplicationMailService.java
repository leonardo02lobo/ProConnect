package Proconnect.backProconnect.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.stereotype.Service;

/**
 * Envío del correo de resolución al postulante con reintentos (3 intentos, backoff exponencial). Un
 * fallo definitivo se registra y se descarta: nunca revierte el cambio de estado, que ya está
 * confirmado.
 */
@Service
public class ApplicationMailService {

    private static final Logger log = LoggerFactory.getLogger(ApplicationMailService.class);

    private final JavaMailSender mailSender;
    private final MailTemplates templates;
    private final String from;

    public ApplicationMailService(
            JavaMailSender mailSender,
            MailTemplates templates,
            @Value("${proconnect.mail.from}") String from) {
        this.mailSender = mailSender;
        this.templates = templates;
        this.from = from;
    }

    @Retryable(
            retryFor = MailException.class,
            maxAttemptsExpression = "${proconnect.mail.retry.max-attempts:3}",
            backoff =
                    @Backoff(
                            delayExpression = "${proconnect.mail.retry.delay-ms:2000}",
                            multiplierExpression = "${proconnect.mail.retry.multiplier:2}"))
    public void sendResolution(PostulationResolvedEvent event) {
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
            var context = RetrySynchronizationManager.getContext();
            int attempt = context == null ? 1 : context.getRetryCount() + 1;
            log.warn(
                    "Fallo SMTP enviando el correo de la postulación {} (intento {}): {}",
                    event.postulationId(),
                    attempt,
                    e.getMessage());
            throw e;
        }
    }

    @Recover
    public void giveUp(MailException e, PostulationResolvedEvent event) {
        log.error(
                "Correo de la postulación {} descartado tras agotar los reintentos",
                event.postulationId(),
                e);
    }
}
