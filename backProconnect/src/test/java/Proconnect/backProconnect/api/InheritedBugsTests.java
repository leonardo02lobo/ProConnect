package Proconnect.backProconnect.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import Proconnect.backProconnect.Entity.Comments;
import Proconnect.backProconnect.Entity.Company;
import Proconnect.backProconnect.Entity.Friend;
import Proconnect.backProconnect.Entity.FriendStatus;
import Proconnect.backProconnect.Entity.Job;
import Proconnect.backProconnect.Entity.Posts;
import Proconnect.backProconnect.Entity.Postulation;
import Proconnect.backProconnect.security.SecurityTestSupport;
import com.jayway.jsonpath.JsonPath;
import jakarta.servlet.http.Cookie;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

/**
 * Regresión de los bugs funcionales heredados (tareas "Bugs heredados" de cleanup-codebase,
 * FINDINGs de risks, perf, e2e y security en BOARD.md).
 */
@SpringBootTest
@AutoConfigureMockMvc
@SuppressWarnings("unchecked")
class InheritedBugsTests extends SecurityTestSupport {

    private Long savePost(Long userId, String title, Instant date) {
        return posts.save(new Posts(null, userId, "contenido", title, null, date)).getId();
    }

    /**
     * B1: accept/reject/cancel fallaban siempre (check invertido) y /pending no daba el id de la
     * solicitud.
     */
    @Test
    void friendRequestsCanBeAcceptedRejectedAndCancelled() throws Exception {
        Long anaId = createUser("ana", "USER");
        Long bobId = createUser("bob", "USER");
        Long carlaId = createUser("carla", "USER");
        Cookie ana = login("ana");
        Cookie bob = login("bob");
        Cookie carla = login("carla");

        mvc.perform(
                        post("/api/friends/requests")
                                .cookie(ana)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"userId\":" + bobId + "}"))
                .andExpect(status().isCreated());
        String pending =
                mvc.perform(get("/api/friends/requests").cookie(bob))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        Integer requestId = JsonPath.read(pending, "$.items[0].id");
        assertThat((Integer) JsonPath.read(pending, "$.items[0].requesterId"))
                .isEqualTo(anaId.intValue());
        mvc.perform(post("/api/friends/requests/" + requestId + "/accept").cookie(bob))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
        mvc.perform(post("/api/friends/requests/" + requestId + "/accept").cookie(bob))
                .andExpect(status().isConflict());
        mvc.perform(get("/api/users/" + anaId + "/friends").cookie(carla))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1));
        mvc.perform(
                        post("/api/friends/requests")
                                .cookie(bob)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"userId\":" + anaId + "}"))
                .andExpect(status().isConflict());
        mvc.perform(delete("/api/friends/" + anaId).cookie(bob)).andExpect(status().isNoContent());
        assertThat(friends.count()).isZero();

        mvc.perform(
                        post("/api/friends/requests")
                                .cookie(carla)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"userId\":" + anaId + "}"))
                .andExpect(status().isCreated());
        Long rejectId = friends.findAll().get(0).getId();
        mvc.perform(post("/api/friends/requests/" + rejectId + "/reject").cookie(ana))
                .andExpect(status().isOk());
        assertThat(friends.findById(rejectId).orElseThrow().getStatus())
                .isEqualTo(FriendStatus.REJECTED);

        Long cancelId =
                friends.save(new Friend(null, carlaId, bobId, FriendStatus.PENDING)).getId();
        mvc.perform(delete("/api/friends/requests/" + cancelId).cookie(bob))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/friends/requests/" + cancelId).cookie(carla))
                .andExpect(status().isNoContent());
        assertThat(friends.existsById(cancelId)).isFalse();
        mvc.perform(
                        post("/api/friends/requests")
                                .cookie(ana)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"userId\":" + anaId + "}"))
                .andExpect(status().isBadRequest());
    }

    /** B2: DELETE de comentario respondía 401 aunque borraba. */
    @Test
    void deletingACommentAnswers204() throws Exception {
        Long anaId = createUser("ana", "USER");
        Cookie ana = login("ana");
        Long postId = savePost(anaId, "t", Instant.now());
        Long commentId =
                comments.save(new Comments(null, postId, anaId, "hola", Instant.now())).getId();
        mvc.perform(delete("/api/comments/" + commentId).cookie(ana))
                .andExpect(status().isNoContent());
        assertThat(comments.existsById(commentId)).isFalse();
        mvc.perform(delete("/api/comments/" + commentId).cookie(ana))
                .andExpect(status().isNotFound());
    }

    /** B3: la actualización de una postulación no se guardaba. */
    @Test
    void postulationUpdatePersists() throws Exception {
        Long carlaId = createUser("carla", "COMPANY_ADMIN");
        createUser("bob", "USER");
        Cookie bob = login("bob");
        Long companyId = companies.save(new Company(null, "ACME", "d", "l", carlaId)).getId();
        Long job1 = jobs.save(new Job(null, companyId, "Dev", "d", "w", Instant.now())).getId();
        Long job2 = jobs.save(new Job(null, companyId, "QA", "d", "w", Instant.now())).getId();
        String created =
                mvc.perform(
                                post("/api/postulations")
                                        .cookie(bob)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content("{\"jobId\":" + job1 + "}"))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.status").value("PENDING"))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        Integer id = JsonPath.read(created, "$.id");
        mvc.perform(
                        put("/api/postulations/" + id)
                                .cookie(bob)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"jobId\":" + job2 + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobId").value(job2));
        Postulation stored = postulations.findById(id.longValue()).orElseThrow();
        assertThat(stored.getJobId()).isEqualTo(job2);
        mvc.perform(
                        post("/api/postulations")
                                .cookie(bob)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"jobId\":" + job2 + "}"))
                .andExpect(status().isConflict());
    }

    /**
     * B4: las fotos de perfil se leían por un GET roto. Desde V3 (restructure-models-performance)
     * avatar y portada son parte del usuario: se ven en GET /api/users/{id}.
     */
    @Test
    void profilePhotosAreReadWithTheUser() throws Exception {
        Long anaId = createUser("ana", "USER");
        createUser("bob", "USER");
        Cookie bob = login("bob");
        setMedia(anaId, "a.png", "w.png");
        mvc.perform(get("/api/users/" + anaId).cookie(bob))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.avatarUrl").value("a.png"))
                .andExpect(jsonPath("$.coverUrl").value("w.png"));
        mvc.perform(get("/api/users/999999").cookie(bob)).andExpect(status().isNotFound());
    }

    /**
     * B5: el feed vacío era null, no tenía orden y duplicaba posts con varias filas de
     * profile_photos (ahora el avatar es una columna de user: no puede duplicar).
     */
    @Test
    void feedIsAnOrderedListWithoutDuplicates() throws Exception {
        Long anaId = createUser("ana", "USER");
        Cookie ana = login("ana");
        mvc.perform(get("/api/posts").cookie(ana))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"items\":[],\"nextCursor\":null}"));

        setMedia(anaId, "new.png", null);
        long now = System.currentTimeMillis();
        savePost(anaId, "viejo", Instant.ofEpochMilli(now - 60_000));
        savePost(anaId, "nuevo", Instant.ofEpochMilli(now));
        String feed =
                mvc.perform(get("/api/posts").cookie(ana))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        List<String> titles = JsonPath.read(feed, "$.items[*].title");
        assertThat(titles).containsExactly("nuevo", "viejo");
        assertThat((String) JsonPath.read(feed, "$.items[0].user.avatarUrl")).isEqualTo("new.png");
    }

    /**
     * B6: perfil 500 con un post sin comentarios; userLiked del perfil usaba al dueño en vez del
     * viewer.
     */
    @Test
    void profileWorksForPostsWithoutCommentsAndUsesTheViewer() throws Exception {
        Long anaId = createUser("ana", "USER");
        Long bobId = createUser("bob", "USER");
        Cookie ana = login("ana");
        Cookie bob = login("bob");
        mvc.perform(get("/api/users/" + bobId + "/profile").cookie(ana))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts.length()").value(0));

        Long withoutComments = savePost(anaId, "sin comentarios", Instant.now());
        Long liked = savePost(anaId, "con like de bob", Instant.now().plusMillis(1000));
        // Por la API: los contadores denormalizados los mantiene el servicio (V4).
        mvc.perform(
                        post("/api/posts/" + liked + "/comments")
                                .cookie(bob)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"content\":\"bien\"}"))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/posts/" + liked + "/likes").cookie(bob))
                .andExpect(status().isCreated());

        String asBob =
                mvc.perform(get("/api/users/" + anaId + "/profile").cookie(bob))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        assertThat((List<Object>) JsonPath.read(asBob, "$.posts")).hasSize(2);
        assertThat(
                        (List<Object>)
                                JsonPath.read(asBob, "$.posts[?(@.id == " + liked + ")].userLiked"))
                .containsExactly(true);
        assertThat(
                        (List<Object>)
                                JsonPath.read(
                                        asBob,
                                        "$.posts[?(@.id == "
                                                + withoutComments
                                                + ")].commentsPreview[*]"))
                .isEmpty();
        assertThat(
                        (List<Object>)
                                JsonPath.read(
                                        asBob, "$.posts[?(@.id == " + liked + ")].commentCount"))
                .containsExactly(1);

        String asAna =
                mvc.perform(get("/api/users/" + anaId + "/profile").cookie(ana))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        assertThat(
                        (List<Object>)
                                JsonPath.read(asAna, "$.posts[?(@.id == " + liked + ")].userLiked"))
                .containsExactly(false);
        mvc.perform(get("/api/users/999999/profile").cookie(ana)).andExpect(status().isNotFound());
    }

    /** B7: user.name era UNIQUE e impedía nombres repetidos. */
    @Test
    void twoUsersCanShareTheSameName() throws Exception {
        for (String suffix : List.of("1", "2")) {
            mvc.perform(
                            post("/api/auth/register")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(
                                            "{\"name\":\"Juan Pérez\",\"userName\":\"juan"
                                                    + suffix
                                                    + "\",\"email\":\"juan"
                                                    + suffix
                                                    + "@example.com\",\"password\":\""
                                                    + PASSWORD
                                                    + "\"}"))
                    .andExpect(status().isCreated());
        }
        assertThat(users.findAll()).filteredOn(u -> u.getName().equals("Juan Pérez")).hasSize(2);
    }
}
