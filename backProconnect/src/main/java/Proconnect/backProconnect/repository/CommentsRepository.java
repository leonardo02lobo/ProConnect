package Proconnect.backProconnect.repository;

import Proconnect.backProconnect.Entity.Comments;
import Proconnect.backProconnect.repository.projection.CommentRow;
import Proconnect.backProconnect.repository.projection.CommentView;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Comentarios. Índice {@code idx_comments_post_feed (post_id, created_at, id)}. */
@Repository
public interface CommentsRepository extends JpaRepository<Comments, Long> {

    String VIEW_SELECT =
            "SELECT new Proconnect.backProconnect.repository.projection.CommentView("
                    + "c.id, c.postId, c.userId, c.content, c.createdAt, u.name, u.userName,"
                    + " u.email, u.role, u.avatarUrl, u.coverUrl) FROM Comments c JOIN c.author u ";

    /**
     * Preview del feed/perfil en UNA query para toda la página: los {@code n} comentarios más
     * recientes de cada post (ROW_NUMBER por post), con su autor, en orden cronológico. Evita el
     * N+1 de una query por post (design D4, Q3).
     */
    @NativeQuery(
            """
            SELECT t.id AS id, t.post_id AS postId, t.user_id AS userId, t.content AS content,
                   t.created_at AS createdAt, t.name AS authorName, t.user_name AS authorUserName,
                   t.email AS authorEmail, t.role AS authorRole, t.avatar_url AS authorAvatarUrl,
                   t.cover_url AS authorCoverUrl
            FROM (
                SELECT c.id, c.post_id, c.user_id, c.content, c.created_at,
                       u.name, u.user_name, u.email, u.role, u.avatar_url, u.cover_url,
                       ROW_NUMBER() OVER (PARTITION BY c.post_id ORDER BY c.created_at DESC, c.id DESC) AS rn
                FROM proconnect.comments c
                JOIN proconnect.user u ON u.id = c.user_id
                WHERE c.post_id IN (:postIds)
            ) t
            WHERE t.rn <= :perPost
            ORDER BY t.post_id, t.created_at, t.id
            """)
    List<CommentRow> latestPerPost(
            @Param("postIds") Collection<Long> postIds, @Param("perPost") int perPost);

    /** Primera página de comentarios de un post (más antiguo primero). */
    @Query(VIEW_SELECT + "WHERE c.postId = :postId ORDER BY c.createdAt, c.id")
    List<CommentView> pageFirst(@Param("postId") Long postId, Limit limit);

    @Query(
            VIEW_SELECT
                    + "WHERE c.postId = :postId AND (c.createdAt > :ts OR (c.createdAt = :ts AND"
                    + " c.id > :id)) ORDER BY c.createdAt, c.id")
    List<CommentView> pageAfter(
            @Param("postId") Long postId,
            @Param("ts") Instant ts,
            @Param("id") Long id,
            Limit limit);

    /** Un comentario concreto con su autor (respuesta del POST). */
    @Query(VIEW_SELECT + "WHERE c.id = :id")
    Optional<CommentView> findView(@Param("id") Long id);
}
