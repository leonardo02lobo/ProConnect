package Proconnect.backProconnect.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import Proconnect.backProconnect.Entity.Comments;
import Proconnect.backProconnect.Entity.Posts;
import Proconnect.backProconnect.Entity.ProfilePhotos;
import jakarta.servlet.http.Cookie;

/** Ninguna vista de terceros expone password, hash, token ni el email ajeno (SEC-13). */
@SpringBootTest
@AutoConfigureMockMvc
class SensitiveDataTests extends SecurityTestSupport {

    @Test
    void thirdPartyViewsDoNotLeakCredentialsOrEmail() throws Exception {
        Long anaId = createUser("ana", "USER");
        createUser("bob", "USER");
        photos.save(new ProfilePhotos(null, anaId, "a.png", null));
        Long postId = posts.save(new Posts(null, anaId, "c", "t", null, new java.util.Date())).getId();
        comments.save(new Comments(null, postId, anaId, "hola", new java.util.Date()));
        Cookie bob = login("bob");

        List<String> urls = List.of("/api/posts", "/api/posts/" + postId, "/api/users", "/api/users/" + anaId,
                "/api/users?q=ana", "/api/users/" + anaId + "/profile", "/api/posts/" + postId + "/comments");
        for (String url : urls) {
            String body = mvc.perform(get(url).cookie(bob)).andExpect(status().isOk()).andReturn().getResponse()
                    .getContentAsString();
            assertThat(body).as(url).doesNotContain("password").doesNotContain("$2a$").doesNotContain("$2b$")
                    .doesNotContainIgnoringCase("token").doesNotContain("pc_session")
                    .doesNotContain("ana@example.com");
        }
        // El propio usuario sí ve su email en /me y en su perfil.
        Cookie ana = login("ana");
        assertThat(mvc.perform(get("/api/auth/me").cookie(ana)).andReturn().getResponse().getContentAsString())
                .contains("ana@example.com");
        assertThat(mvc.perform(get("/api/users/" + anaId + "/profile").cookie(ana)).andReturn().getResponse()
                .getContentAsString()).contains("ana@example.com");
    }
}
