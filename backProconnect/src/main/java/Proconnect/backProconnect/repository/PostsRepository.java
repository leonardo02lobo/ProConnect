package Proconnect.backProconnect.repository;

import Proconnect.backProconnect.Entity.Posts;
import Proconnect.backProconnect.repository.projection.FeedRow;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostsRepository extends JpaRepository<Posts, Long> {

    /**
     * Feed (o posts de un autor si {@code authorId} no es null) con autor, contadores y si el
     * viewer dio like. Una sola fila de foto por usuario (la más reciente) para no duplicar posts,
     * y orden determinista (más nuevo primero). Optimización (contadores desnormalizados, índices,
     * paginación): restructure-models-performance.
     */
    @Query(
            value =
                    """
            SELECT
                pos.id AS id, pos.title AS title, pos.content AS content, pos.created_at AS date, pos.image_url AS imageUrl,
                us.id AS userId, us.email AS userEmail, us.name AS userName, us.role AS userRole,
                us.user_name AS userUserName,
                prof.photo_url AS photoUrl, prof.wall_photo_url AS wallPhotoUrl,
                (SELECT COUNT(*) FROM proconnect.likes WHERE post_id = pos.id) AS totalLikes,
                (SELECT COUNT(*) FROM proconnect.comments WHERE post_id = pos.id) AS totalComments,
                (SELECT COUNT(*) FROM proconnect.likes WHERE post_id = pos.id AND user_id = :viewerId) AS userLiked
            FROM proconnect.posts AS pos
            JOIN proconnect.user AS us ON pos.user_id = us.id
            LEFT JOIN proconnect.profile_photos AS prof ON prof.id =
                (SELECT MAX(p2.id) FROM proconnect.profile_photos p2 WHERE p2.user_id = us.id)
            WHERE (:authorId IS NULL OR pos.user_id = :authorId)
            ORDER BY pos.created_at DESC, pos.id DESC
            """,
            nativeQuery = true)
    List<FeedRow> findFeed(@Param("viewerId") Long viewerId, @Param("authorId") Long authorId);
}
