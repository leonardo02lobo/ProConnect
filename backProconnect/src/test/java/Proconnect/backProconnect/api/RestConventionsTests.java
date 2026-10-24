package Proconnect.backProconnect.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.jayway.jsonpath.JsonPath;

import Proconnect.backProconnect.security.SecurityTestSupport;
import jakarta.servlet.http.Cookie;

/** Spec rest-api-conventions: recursos en plural, verbos HTTP, 201 + Location, 200 con recurso y 204. */
@SpringBootTest
@AutoConfigureMockMvc
class RestConventionsTests extends SecurityTestSupport {

    private static final Set<String> VERBS = Set.of("create", "edit", "delete", "find", "list", "update", "search");

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    RequestMappingHandlerMapping mapping;

    @Test
    void noRouteContainsAVerb() {
        mapping.getHandlerMethods().keySet().stream().flatMap(info -> info.getPatternValues().stream())
                .filter(p -> p.startsWith("/api"))
                .forEach(p -> {
                    for (String segment : p.split("/")) {
                        assertThat(VERBS).as("ruta %s", p).doesNotContain(segment.toLowerCase());
                    }
                });
    }

    @Test
    void postCrudReturnsResources() throws Exception {
        createUser("ana", "USER");
        Cookie ana = login("ana");
        String created = mvc.perform(post("/api/posts").cookie(ana).contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Hola\",\"content\":\"Mundo\",\"imageUrl\":\"x.png\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("Hola"))
                .andReturn().getResponse().getContentAsString();
        Integer id = JsonPath.read(created, "$.id");
        mvc.perform(get("/api/posts/" + id).cookie(ana)).andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Mundo"));
        mvc.perform(put("/api/posts/" + id).cookie(ana).contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Hola2\",\"content\":\"Mundo2\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title").value("Hola2"));
        mvc.perform(delete("/api/posts/" + id).cookie(ana)).andExpect(status().isNoContent());
        mvc.perform(get("/api/posts/" + id).cookie(ana)).andExpect(status().isNotFound());
    }

    @Test
    void createSetsLocationHeader() throws Exception {
        createUser("ana", "USER");
        Cookie ana = login("ana");
        String body = mvc.perform(post("/api/posts").cookie(ana).contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"t\",\"content\":\"c\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        Integer id = JsonPath.read(body, "$.id");
        mvc.perform(post("/api/posts").cookie(ana).contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"t2\",\"content\":\"c2\"}"))
                .andExpect(header().string("Location", "/api/posts/" + (id + 1)));
    }

    @Test
    void likesAndCommentsAreNestedAndDeleteHasNoBody() throws Exception {
        Long anaId = createUser("ana", "USER");
        createUser("bob", "USER");
        Cookie bob = login("bob");
        Long postId = posts.save(new Proconnect.backProconnect.Entity.Posts(null, anaId, "c", "t", null,
                new java.util.Date())).getId();

        mvc.perform(post("/api/posts/" + postId + "/likes").cookie(bob)).andExpect(status().isCreated());
        mvc.perform(get("/api/posts/" + postId + "/likes").cookie(bob)).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
        // DELETE sin body: el like se identifica por la ruta y la sesión.
        mvc.perform(delete("/api/posts/" + postId + "/likes").cookie(bob)).andExpect(status().isNoContent());
        assertThat(likes.count()).isZero();
        mvc.perform(delete("/api/posts/" + postId + "/likes").cookie(bob)).andExpect(status().isNotFound());

        String comment = mvc.perform(post("/api/posts/" + postId + "/comments").cookie(bob)
                .contentType(MediaType.APPLICATION_JSON).content("{\"content\":\"hola\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.name").value("bob"))
                .andExpect(jsonPath("$.postId").value(postId))
                .andReturn().getResponse().getContentAsString();
        Integer commentId = JsonPath.read(comment, "$.id");
        mvc.perform(get("/api/posts/" + postId + "/comments").cookie(bob)).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("hola"));
        mvc.perform(post("/api/posts/999999/comments").cookie(bob).contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"x\"}")).andExpect(status().isNotFound());
        mvc.perform(delete("/api/comments/" + commentId).cookie(bob)).andExpect(status().isNoContent());
    }
}
