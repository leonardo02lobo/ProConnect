package Proconnect.backProconnect.mail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import Proconnect.backProconnect.Entity.Company;
import Proconnect.backProconnect.Entity.Job;
import Proconnect.backProconnect.Entity.Postulation;
import Proconnect.backProconnect.Entity.PostulationStatus;
import Proconnect.backProconnect.security.SecurityTestSupport;
import java.time.Duration;
import java.util.Date;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

/**
 * SMTP caído: el estado se confirma igual, la API responde 200 y el log registra los 3 intentos
 * fallidos.
 */
@SpringBootTest(properties = "spring.mail.port=3999")
@AutoConfigureMockMvc
@ExtendWith(OutputCaptureExtension.class)
class MailFailureTests extends SecurityTestSupport {

    @Test
    void smtpDownDoesNotRollBackTheResolution(CapturedOutput output) throws Exception {
        Long ownerId = createUser("carla", "COMPANY_ADMIN");
        Long anaId = createUser("ana", "USER");
        Long companyId = companies.save(new Company(null, "Acme", "d", "l", ownerId)).getId();
        Long jobId =
                jobs.save(new Job(null, companyId, "Backend Dev", "d", "w", new Date())).getId();
        Long id = postulations.save(new Postulation(null, anaId, jobId, new Date())).getId();

        mvc.perform(post("/api/postulations/" + id + "/accept").cookie(login("carla")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
        assertThat(postulations.findById(id).orElseThrow().getStatus())
                .isEqualTo(PostulationStatus.ACCEPTED);

        await().atMost(Duration.ofSeconds(30))
                .untilAsserted(
                        () ->
                                assertThat(output.getOut())
                                        .contains("postulación " + id + " (intento 1)")
                                        .contains("postulación " + id + " (intento 2)")
                                        .contains("postulación " + id + " (intento 3)")
                                        .contains(
                                                "Correo de la postulación "
                                                        + id
                                                        + " descartado tras agotar los reintentos"));
        assertThat(postulations.findById(id).orElseThrow().getStatus())
                .isEqualTo(PostulationStatus.ACCEPTED);
    }
}
