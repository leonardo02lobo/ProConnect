package Proconnect.backProconnect.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import Proconnect.backProconnect.Entity.Posts;
import com.jayway.jsonpath.JsonPath;
import jakarta.servlet.http.Cookie;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Umbral bloqueante del change (spec feed-performance / performance-baseline): el feed y el perfil
 * ejecutan como mucho 4 sentencias SQL por petición, independientemente de cuántos posts,
 * comentarios y likes tenga la página. Si un cambio introduce un N+1 (p. ej. cargar el autor de
 * cada comentario con su propia query), estos tests fallan en CI. Cuenta sentencias DML que llegan
 * al driver (datasource-proxy), contra MySQL real con el esquema de Flyway.
 */
class FeedQueryCountIT extends MySqlIntegrationTest {

    private static final int MAX_STATEMENTS = 4;
    private static final Instant T0 = Instant.parse("2026-01-01T00:00:00Z");

    @Autowired JdbcTemplate jdbc;

    private Long anaId;
    private Long bobId;
    private Cookie bob;

    @BeforeEach
    void dataset() throws Exception {
        anaId = createUser("ana", "USER");
        bobId = createUser("bob", "USER");
        bob = login("bob");
    }

    /** {@code n} posts de un autor con fechas crecientes (el último es el más nuevo). */
    private List<Long> insertPosts(Long authorId, int n, int offsetSeconds) {
        List<Object[]> rows = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            rows.add(
                    new Object[] {
                        authorId,
                        "c" + i,
                        "post " + (offsetSeconds + i),
                        Timestamp.from(T0.plusSeconds(offsetSeconds + i))
                    });
        }
        jdbc.batchUpdate(
                "INSERT INTO posts (user_id, content, title, created_at) VALUES (?, ?, ?, ?)",
                rows);
        return jdbc.queryForList(
                "SELECT id FROM posts WHERE user_id = ? ORDER BY created_at, id",
                Long.class,
                authorId);
    }

    private void insertComments(List<Long> postIds, int perPost) {
        List<Object[]> rows = new ArrayList<>();
        for (Long postId : postIds) {
            for (int i = 0; i < perPost; i++) {
                rows.add(
                        new Object[] {
                            postId,
                            i % 2 == 0 ? anaId : bobId,
                            "comentario " + i,
                            Timestamp.from(T0.plusSeconds(10_000 + i))
                        });
            }
        }
        jdbc.batchUpdate(
                "INSERT INTO comments (post_id, user_id, content, created_at) VALUES (?, ?, ?, ?)",
                rows);
        jdbc.update(
                "UPDATE posts p SET comment_count = (SELECT COUNT(*) FROM comments c WHERE"
                        + " c.post_id = p.id)");
    }

    private String getCounted(String url, long[] statements) throws Exception {
        // Petición previa sin contar: calienta lo que es por sesión y no por página (p. ej. el
        // rol cacheado que resuelve el filtro JWT en add-role-management).
        mvc.perform(get("/api/auth/me").cookie(bob)).andExpect(status().isOk());
        QueryCounter.reset();
        String body =
                mvc.perform(get(url).cookie(bob))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        statements[0] = QueryCounter.dml();
        return body;
    }

    /** Spec: página de 20 posts que en total tienen 500 comentarios → ≤ 4 sentencias. */
    @Test
    void feedPageWithManyCommentsRunsAtMostFourStatements() throws Exception {
        List<Long> ids = insertPosts(anaId, 25, 0);
        insertComments(ids.subList(5, 25), 25); // los 20 más recientes: 500 comentarios
        for (Long id : ids.subList(15, 25)) {
            mvc.perform(post("/api/posts/" + id + "/likes").cookie(bob))
                    .andExpect(status().isCreated());
        }

        long[] n = new long[1];
        String page1 = getCounted("/api/posts?limit=20", n);
        assertThat(n[0]).as("sentencias del feed").isLessThanOrEqualTo(MAX_STATEMENTS);
        assertThat(n[0]).as("feed en exactamente 3 queries (D4)").isEqualTo(3);

        List<Integer> pageIds = JsonPath.read(page1, "$.items[*].id");
        assertThat(pageIds).hasSize(20);
        assertThat((String) JsonPath.read(page1, "$.nextCursor")).isNotBlank();
        // Orden: del más nuevo al más antiguo.
        assertThat(pageIds.get(0).longValue()).isEqualTo(ids.get(24));
        List<Integer> previewSizes = JsonPath.read(page1, "$.items[*].commentsPreview.length()");
        assertThat(previewSizes).allMatch(size -> size <= 3).contains(3);
        List<Integer> commentCounts = JsonPath.read(page1, "$.items[*].commentCount");
        assertThat(commentCounts).containsOnly(25);
        List<Boolean> liked = JsonPath.read(page1, "$.items[0:10].userLiked");
        assertThat(liked).containsOnly(true);
        // El preview son los 3 MÁS RECIENTES, en orden cronológico.
        List<String> preview = JsonPath.read(page1, "$.items[0].commentsPreview[*].content");
        assertThat(preview).containsExactly("comentario 22", "comentario 23", "comentario 24");
        List<String> authors = JsonPath.read(page1, "$.items[0].commentsPreview[*].user.name");
        assertThat(authors).containsExactly("ana", "bob", "ana");
    }

    /** Spec: última página con menos de limit posts → los restantes y nextCursor null. */
    @Test
    void cursorPaginationIsCompleteAndStableUnderInserts() throws Exception {
        List<Long> ids = insertPosts(anaId, 45, 0);
        long[] n = new long[1];
        String page1 = getCounted("/api/posts?limit=20", n);
        String cursor = JsonPath.read(page1, "$.nextCursor");

        // Un post nuevo mientras se pagina: no debe repetir ni omitir posts de la secuencia.
        posts.save(new Posts(null, bobId, "c", "nuevo", null, Instant.now()));

        Set<Integer> seen = new HashSet<>(JsonPath.read(page1, "$.items[*].id"));
        String page2 = getCounted("/api/posts?limit=20&cursor=" + cursor, n);
        assertThat(n[0]).isLessThanOrEqualTo(MAX_STATEMENTS);
        List<Integer> ids2 = JsonPath.read(page2, "$.items[*].id");
        assertThat(ids2).hasSize(20).doesNotContainAnyElementsOf(seen);
        seen.addAll(ids2);
        String page3 =
                getCounted("/api/posts?limit=20&cursor=" + JsonPath.read(page2, "$.nextCursor"), n);
        List<Integer> ids3 = JsonPath.read(page3, "$.items[*].id");
        assertThat(ids3).hasSize(5).doesNotContainAnyElementsOf(seen);
        assertThat((Object) JsonPath.read(page3, "$.nextCursor")).isNull();
        seen.addAll(ids3);
        assertThat(seen).hasSize(45);
        assertThat(seen.stream().map(Integer::longValue).toList())
                .containsExactlyInAnyOrderElementsOf(ids);
    }

    /** Spec: limit=1000 → como mucho 50 posts. Cursor manipulado → 400. */
    @Test
    void limitIsCappedAndBadCursorIs400() throws Exception {
        insertPosts(anaId, 60, 0);
        long[] n = new long[1];
        String page = getCounted("/api/posts?limit=1000", n);
        assertThat((List<Object>) JsonPath.read(page, "$.items")).hasSize(50);
        mvc.perform(get("/api/posts?cursor=not-a-cursor").cookie(bob))
                .andExpect(status().isBadRequest());
    }

    /** Spec: usuario con 200 posts → 20 posts, nextCursor y ≤ 4 sentencias (sin cartesiano). */
    @Test
    void profileWith200PostsRunsAtMostFourStatements() throws Exception {
        List<Long> ids = insertPosts(anaId, 200, 0);
        insertComments(ids.subList(180, 200), 10);
        insertPosts(bobId, 30, 500); // posts de otro autor: no deben aparecer

        long[] n = new long[1];
        String profile = getCounted("/api/users/" + anaId + "/profile", n);
        assertThat(n[0]).as("sentencias del perfil").isLessThanOrEqualTo(MAX_STATEMENTS);
        assertThat((List<Object>) JsonPath.read(profile, "$.posts")).hasSize(20);
        assertThat((String) JsonPath.read(profile, "$.nextCursor")).isNotBlank();
        List<Integer> authors = JsonPath.read(profile, "$.posts[*].userId");
        assertThat(authors).containsOnly(anaId.intValue());

        String next =
                getCounted(
                        "/api/users/"
                                + anaId
                                + "/posts?cursor="
                                + JsonPath.read(profile, "$.nextCursor"),
                        n);
        assertThat(n[0]).isLessThanOrEqualTo(MAX_STATEMENTS);
        List<Integer> nextIds = JsonPath.read(next, "$.items[*].id");
        assertThat(nextIds)
                .hasSize(20)
                .doesNotContainAnyElementsOf(JsonPath.read(profile, "$.posts[*].id"));
    }

    /** Lista completa de comentarios paginada: 1 sentencia por página. */
    @Test
    void commentsPageIsOneStatement() throws Exception {
        List<Long> ids = insertPosts(anaId, 1, 0);
        insertComments(ids, 30);
        long[] n = new long[1];
        String page1 = getCounted("/api/posts/" + ids.get(0) + "/comments?limit=20", n);
        assertThat(n[0]).isEqualTo(1);
        List<String> contents = JsonPath.read(page1, "$.items[*].content");
        assertThat(contents).hasSize(20).startsWith("comentario 0");
        String page2 =
                getCounted(
                        "/api/posts/"
                                + ids.get(0)
                                + "/comments?limit=20&cursor="
                                + JsonPath.read(page1, "$.nextCursor"),
                        n);
        assertThat((List<Object>) JsonPath.read(page2, "$.items")).hasSize(10);
        assertThat((Object) JsonPath.read(page2, "$.nextCursor")).isNull();
    }

    /** Spec: like y unlike → likeCount sube 1 y vuelve, y coincide con COUNT(*) de likes. */
    @Test
    void likeCountersStayConsistent() throws Exception {
        Long postId = insertPosts(anaId, 1, 0).get(0);
        Cookie ana = login("ana");
        mvc.perform(post("/api/posts/" + postId + "/likes").cookie(bob))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/posts/" + postId + "/likes").cookie(ana))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/posts/" + postId + "/likes").cookie(bob))
                .andExpect(status().isConflict());
        assertThat(likeCount(postId)).isEqualTo(2).isEqualTo(realLikes(postId));
        mvc.perform(delete("/api/posts/" + postId + "/likes").cookie(bob))
                .andExpect(status().isNoContent());
        mvc.perform(delete("/api/posts/" + postId + "/likes").cookie(bob))
                .andExpect(status().isNotFound());
        assertThat(likeCount(postId)).isEqualTo(1).isEqualTo(realLikes(postId));

        String feed = getCounted("/api/posts?limit=5", new long[1]);
        List<Integer> counts = JsonPath.read(feed, "$.items[?(@.id == " + postId + ")].likeCount");
        assertThat(counts).containsExactly(1);
    }

    private int likeCount(Long postId) {
        return jdbc.queryForObject(
                "SELECT like_count FROM posts WHERE id = ?", Integer.class, postId);
    }

    private int realLikes(Long postId) {
        return jdbc.queryForObject(
                "SELECT COUNT(*) FROM likes WHERE post_id = ?", Integer.class, postId);
    }
}
