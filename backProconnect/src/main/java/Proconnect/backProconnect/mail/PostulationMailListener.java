package Proconnect.backProconnect.mail;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/** Envía el correo solo después del commit y fuera del hilo del request. */
@Component
public class PostulationMailListener {

    private final ApplicationMailService mailService;

    public PostulationMailListener(ApplicationMailService mailService) {
        this.mailService = mailService;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onResolved(PostulationResolvedEvent event) {
        mailService.sendResolution(event);
    }
}
