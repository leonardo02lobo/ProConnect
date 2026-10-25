package Proconnect.backProconnect.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import Proconnect.backProconnect.Entity.Posts;
import Proconnect.backProconnect.security.SecurityTestSupport;
import com.jayway.jsonpath.JsonPath;
import jakarta.servlet.http.Cookie;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Spec api-error-handling: códigos correctos, problem+json uniforme y 500 sin detalles internos.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(ApiErrorHandlingTests.BoomConfig.class)
@ExtendWith(OutputCaptureExtension.class)
class ApiErrorHandlingTests extends SecurityTestSupport {

    /**
     * Controller solo de test que simula un fallo inesperado con detalles internos en el mensaje.
     */
    @TestConfiguration
    static class BoomConfig {
        @RestController
        static class BoomController {
            @GetMapping("/api/test-only/boom")
            String boom() {
                throw new IllegalStateException(
                        "could not execute statement; SQL [select * from proconnect.user]");
            }
        }
    }

    @Test
    void missingPostIs404ProblemJson() throws Exception {
        createUser("ana", "USER");
        Cookie ana = login("ana");
        mvc.perform(get("/api/posts/999999").cookie(ana))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("about:blank"))
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").isNotEmpty())
                .andExpect(jsonPath("$.instance").value("/api/posts/999999"))
                .andExpect(jsonPath("$.traceId").isNotEmpty());
    }

    @Test
    void invalidBodyIs400WithOneErrorPerField() throws Exception {
        createUser("ana", "USER");
        Cookie ana = login("ana");
        mvc.perform(
                        post("/api/posts")
                                .cookie(ana)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"title\":\"t\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.length()").value(1))
                .andExpect(jsonPath("$.errors[0].field").value("content"))
                .andExpect(jsonPath("$.errors[0].message").isNotEmpty());
        mvc.perform(
                        post("/api/posts")
                                .cookie(ana)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.length()").value(2));
        mvc.perform(
                        post("/api/posts")
                                .cookie(ana)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{not json"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
        assertThat(posts.count()).isZero();
    }

    @Test
    void duplicateLikeIs409() throws Exception {
        Long anaId = createUser("ana", "USER");
        Cookie ana = login("ana");
        Long postId =
                posts.save(new Posts(null, anaId, "c", "t", null, java.time.Instant.now())).getId();
        mvc.perform(post("/api/posts/" + postId + "/likes").cookie(ana))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/posts/" + postId + "/likes").cookie(ana))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(409));
        assertThat(likes.count()).isEqualTo(1);
    }

    @Test
    void unexpectedErrorIsGeneric500AndLoggedWithTraceId(CapturedOutput output) throws Exception {
        createUser("ana", "USER");
        Cookie ana = login("ana");
        MvcResult res =
                mvc.perform(get("/api/test-only/boom").cookie(ana))
                        .andExpect(status().isInternalServerError())
                        .andExpect(
                                content()
                                        .contentTypeCompatibleWith(
                                                MediaType.APPLICATION_PROBLEM_JSON))
                        .andExpect(jsonPath("$.detail").value("Error interno del servidor"))
                        .andReturn();
        String body = res.getResponse().getContentAsString();
        assertThat(body)
                .doesNotContain("SQL")
                .doesNotContain("proconnect.user")
                .doesNotContain("IllegalState")
                .doesNotContain("at Proconnect")
                .doesNotContain("Exception");
        String traceId = JsonPath.read(body, "$.traceId");
        assertThat(traceId).isNotBlank().isEqualTo(res.getResponse().getHeader("X-Trace-Id"));
        // El log del servidor sí tiene el stack trace, asociado al mismo traceId.
        assertThat(output.getOut())
                .contains(traceId)
                .contains("IllegalStateException")
                .contains("could not execute statement");
    }

    @Test
    void securityErrorsAreProblemJsonToo() throws Exception {
        mvc.perform(get("/api/posts"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.traceId").isNotEmpty())
                .andExpect(header().exists("X-Trace-Id"));
        createUser("ana", "USER");
        Cookie ana = login("ana");
        mvc.perform(
                        post("/api/posts")
                                .cookie(ana)
                                .header("Origin", "https://evil.example")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"title\":\"t\",\"content\":\"c\"}"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
        mvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"name\":\"x\",\"userName\":\"x\",\"email\":\"no-es-email\",\"password\":\"123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[?(@.field == 'email')]").exists())
                .andExpect(jsonPath("$.errors[?(@.field == 'password')]").exists());
    }

    /**
     * Pendiente de verify-web-security: imageUrl/avatarUrl solo admiten nombres de archivo de
     * media.
     */
    @Test
    void imageReferencesMustBeMediaFileNames() throws Exception {
        Long anaId = createUser("ana", "USER");
        Cookie ana = login("ana");
        for (String bad :
                List.of(
                        "../../etc/passwd.png",
                        "https://evil.example/x.png",
                        "javascript:alert(1)",
                        "x.svg",
                        "a/b.png")) {
            mvc.perform(
                            post("/api/posts")
                                    .cookie(ana)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(
                                            "{\"title\":\"t\",\"content\":\"c\",\"imageUrl\":\""
                                                    + bad
                                                    + "\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors[0].field").value("imageUrl"));
            mvc.perform(
                            put("/api/users/" + anaId + "/media")
                                    .cookie(ana)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{\"avatarUrl\":\"" + bad + "\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors[0].field").value("avatarUrl"));
        }
        mvc.perform(
                        post("/api/posts")
                                .cookie(ana)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"title\":\"t\",\"content\":\"c\",\"imageUrl\":"
                                                + "\"3f2a9c1e-7b4d-4e8a-9f00-1a2b3c4d5e6f.webp\"}"))
                .andExpect(status().isCreated());
        mvc.perform(
                        put("/api/users/" + anaId + "/media")
                                .cookie(ana)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"avatarUrl\":\"avatar.PNG\",\"coverUrl\":null}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.avatarUrl").value("avatar.PNG"));
        assertThat(posts.count()).isEqualTo(1);
    }
}
