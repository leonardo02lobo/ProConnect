package Proconnect.backProconnect.repository;

import Proconnect.backProconnect.Entity.Comments;
import Proconnect.backProconnect.repository.projection.CommentRow;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentsRepository extends JpaRepository<Comments, Long> {

    /**
     * Comentarios de un post con su autor (una fila de foto por usuario), del más antiguo al más
     * nuevo.
     */
    @Query(
            value =
                    """
            SELECT
                com.id AS id, com.post_id AS postId, com.user_id AS userId, com.content AS content, com.created_at AS date,
                us.email AS userEmail, us.name AS userName, us.role AS userRole, us.user_name AS userUserName,
                prof.photo_url AS photoUrl, prof.wall_photo_url AS wallPhotoUrl
            FROM proconnect.comments AS com
            JOIN proconnect.user AS us ON com.user_id = us.id
            LEFT JOIN proconnect.profile_photos AS prof ON prof.id =
                (SELECT MAX(p2.id) FROM proconnect.profile_photos p2 WHERE p2.user_id = us.id)
            WHERE com.post_id = :postId
            ORDER BY com.created_at ASC, com.id ASC
            """,
            nativeQuery = true)
    List<CommentRow> findWithAuthorByPostId(@Param("postId") Long postId);

    /** Un comentario concreto con su autor (respuesta del POST). */
    @Query(
            value =
                    """
            SELECT
                com.id AS id, com.post_id AS postId, com.user_id AS userId, com.content AS content, com.created_at AS date,
                us.email AS userEmail, us.name AS userName, us.role AS userRole, us.user_name AS userUserName,
                prof.photo_url AS photoUrl, prof.wall_photo_url AS wallPhotoUrl
            FROM proconnect.comments AS com
            JOIN proconnect.user AS us ON com.user_id = us.id
            LEFT JOIN proconnect.profile_photos AS prof ON prof.id =
                (SELECT MAX(p2.id) FROM proconnect.profile_photos p2 WHERE p2.user_id = us.id)
            WHERE com.id = :id
            """,
            nativeQuery = true)
    List<CommentRow> findWithAuthorById(@Param("id") Long id);

    void deleteByPostId(Long postId);
}
