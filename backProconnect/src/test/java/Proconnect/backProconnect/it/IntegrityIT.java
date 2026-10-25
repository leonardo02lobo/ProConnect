package Proconnect.backProconnect.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import Proconnect.backProconnect.Entity.Posts;
import jakarta.servlet.http.Cookie;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;

/** Integridad garantizada por la base (spec data-model-integrity), contra MySQL real. */
class IntegrityIT extends MySqlIntegrationTest {

    @Autowired JdbcTemplate jdbc;

    private Long newPost(Long authorId) {
        return posts.save(new Posts(null, authorId, "contenido", "titulo", null, null)).getId();
    }

    /** Tarea 3.3: dos likes simultáneos del mismo usuario → un solo registro, un 201 y un 409. */
    @Test
    void concurrentDoubleLikePersistsOneRowAndReturns409() throws Exception {
        Long anaId = createUser("ana", "USER");
        Long postId = newPost(anaId);
        createUser("bob", "USER");
        Cookie bob = login("bob");

        for (int round = 0; round < 5; round++) {
            Long target = round == 0 ? postId : newPost(anaId);
            CountDownLatch start = new CountDownLatch(1);
            ExecutorService pool = Executors.newFixedThreadPool(2);
            List<Future<Integer>> results = new ArrayList<>();
            for (int i = 0; i < 2; i++) {
                results.add(
                        pool.submit(
                                () -> {
                                    start.await();
                                    return mvc.perform(
                                                    post("/api/posts/" + target + "/likes")
                                                            .cookie(bob))
                                            .andReturn()
                                            .getResponse()
                                            .getStatus();
                                }));
            }
            start.countDown();
            List<Integer> statuses = new ArrayList<>();
            for (Future<Integer> f : results) {
                statuses.add(f.get(30, TimeUnit.SECONDS));
            }
            pool.shutdown();
            Collections.sort(statuses);
            assertThat(statuses).containsExactly(201, 409);
            assertThat(
                            jdbc.queryForObject(
                                    "SELECT COUNT(*) FROM likes WHERE post_id = ?",
                                    Integer.class,
                                    target))
                    .isEqualTo(1);
        }
    }

    @Test
    void likeOnMissingPostIs404() throws Exception {
        createUser("ana", "USER");
        mvc.perform(post("/api/posts/987654321/likes").cookie(login("ana")))
                .andExpect(status().isNotFound());
    }

    /** Borrar un post elimina también sus likes y comentarios (ON DELETE CASCADE). */
    @Test
    void deletingPostCascadesToLikesAndComments() throws Exception {
        Long anaId = createUser("ana", "USER");
        createUser("bob", "USER");
        Cookie ana = login("ana");
        Cookie bob = login("bob");
        Long postId = newPost(anaId);
        mvc.perform(post("/api/posts/" + postId + "/likes").cookie(bob))
                .andExpect(status().isCreated());
        mvc.perform(
                        post("/api/posts/" + postId + "/comments")
                                .cookie(bob)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"content\":\"hola\"}"))
                .andExpect(status().isCreated());

        mvc.perform(delete("/api/posts/" + postId).cookie(ana)).andExpect(status().isNoContent());

        assertThat(
                        jdbc.queryForObject(
                                "SELECT COUNT(*) FROM likes WHERE post_id = ?",
                                Integer.class,
                                postId))
                .isZero();
        assertThat(
                        jdbc.queryForObject(
                                "SELECT COUNT(*) FROM comments WHERE post_id = ?",
                                Integer.class,
                                postId))
                .isZero();
    }

    /** La BD rechaza filas huérfanas aunque la app no las valide. */
    @Test
    void databaseRejectsOrphanRows() {
        Long anaId;
        try {
            anaId = createUser("ana", "USER");
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        org.assertj.core.api.Assertions.assertThatThrownBy(
                        () ->
                                jdbc.update(
                                        "INSERT INTO likes (user_id, post_id) VALUES (?, ?)",
                                        anaId,
                                        987654321L))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }
}
