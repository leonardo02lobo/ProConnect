package Proconnect.backProconnect.mail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import Proconnect.backProconnect.Entity.Company;
import Proconnect.backProconnect.Entity.Job;
import Proconnect.backProconnect.Entity.Postulation;
import Proconnect.backProconnect.Entity.PostulationStatus;
import Proconnect.backProconnect.security.SecurityTestSupport;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.Cookie;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;

/**
 * Spec job-application-notifications: resolución por la empresa y correo al postulante (GreenMail
 * en :3025).
 */
@SpringBootTest
@AutoConfigureMockMvc
class MailNotificationTests extends SecurityTestSupport {

    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP);

    Long postulationId;
    Cookie owner;
    Cookie applicant;
    Cookie otherCompany;
    Cookie admin;

    @BeforeEach
    void data() throws Exception {
        Long ownerId = createUser("Carla", "COMPANY_ADMIN");
        Long anaId = createUser("Ana", "USER");
        createUser("Dave", "COMPANY_ADMIN");
        createUser("Root", "ADMIN");
        Long companyId = companies.save(new Company(null, "Acme", "d", "l", ownerId)).getId();
        Long jobId =
                jobs.save(new Job(null, companyId, "Backend Dev", "d", "w", Instant.now())).getId();
        postulationId =
                postulations.save(new Postulation(null, anaId, jobId, Instant.now())).getId();
        owner = login("Carla");
        applicant = login("Ana");
        otherCompany = login("Dave");
        admin = login("Root");
    }

    @Test
    void newPostulationIsPending() {
        assertThat(postulations.findById(postulationId).orElseThrow().getStatus())
                .isEqualTo(PostulationStatus.PENDING);
    }

    @Test
    void ownerAcceptsAndApplicantGetsThePositiveMail() throws Exception {
        // Un destinatario en el body se ignora: el correo va al email registrado del postulante.
        mvc.perform(
                        post("/api/postulations/" + postulationId + "/accept")
                                .cookie(owner)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"correoDestino\":\"attacker@evil.example\",\"email\":\"attacker@evil.example\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
        assertThat(postulations.findById(postulationId).orElseThrow().getStatus())
                .isEqualTo(PostulationStatus.ACCEPTED);

        assertThat(greenMail.waitForIncomingEmail(10_000, 1)).isTrue();
        MimeMessage[] received = greenMail.getReceivedMessages();
        assertThat(received).hasSize(1);
        MimeMessage mail = received[0];
        assertThat(mail.getAllRecipients())
                .extracting(Object::toString)
                .containsExactly("Ana@example.com");
        assertThat(mail.getSubject()).isEqualTo("Respuesta hacia el puesto de: Backend Dev");
        assertThat(((String) mail.getContent()))
                .contains("Hola Ana")
                .contains("Backend Dev")
                .contains("Acme")
                .contains("Felicidades");
    }

    @Test
    void rejectSendsTheNegativeTemplate() throws Exception {
        mvc.perform(post("/api/postulations/" + postulationId + "/reject").cookie(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
        assertThat(greenMail.waitForIncomingEmail(10_000, 1)).isTrue();
        assertThat(((String) greenMail.getReceivedMessages()[0].getContent()))
                .contains("hemos decidido continuar con otros candidatos")
                .doesNotContain("Felicidades");
    }

    @Test
    void nonOwnersCannotResolveAndNoMailIsSent() throws Exception {
        // El postulante ve su postulación pero no es dueño de la empresa: 403.
        mvc.perform(post("/api/postulations/" + postulationId + "/accept").cookie(applicant))
                .andExpect(status().isForbidden());
        // Otro COMPANY_ADMIN no puede verla (recurso privado, verify-web-security): 404 como un id
        // inexistente.
        mvc.perform(post("/api/postulations/" + postulationId + "/reject").cookie(otherCompany))
                .andExpect(status().isNotFound());
        assertThat(postulations.findById(postulationId).orElseThrow().getStatus())
                .isEqualTo(PostulationStatus.PENDING);
        assertThat(greenMail.waitForIncomingEmail(1_500, 1)).isFalse();
    }

    @Test
    void resolvedPostulationCannotBeResolvedAgain() throws Exception {
        mvc.perform(post("/api/postulations/" + postulationId + "/accept").cookie(owner))
                .andExpect(status().isOk());
        mvc.perform(post("/api/postulations/" + postulationId + "/reject").cookie(owner))
                .andExpect(status().isConflict());
        assertThat(postulations.findById(postulationId).orElseThrow().getStatus())
                .isEqualTo(PostulationStatus.ACCEPTED);
        assertThat(greenMail.waitForIncomingEmail(10_000, 1)).isTrue();
        Thread.sleep(500);
        assertThat(greenMail.getReceivedMessages()).hasSize(1);
    }
}
