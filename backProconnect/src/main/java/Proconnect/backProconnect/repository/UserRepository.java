package Proconnect.backProconnect.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import Proconnect.backProconnect.Entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> findByUserName(String userName);

    Optional<User> findByEmail(String email);

    @Query(value = """
                        SELECT
                us.id,
                us.email,
                us.name,
                us.user_name,
                us.role,
                pr.photo_url,
                pr.wall_photo_url,
                pos.id as post_id,
                pos.title,
                pos.content as post_content,
                pos.date as post_date,
                pos.image_url as post_image,
                com.content as contenidoComentario, com.date as fechaComentario, com.id as id_com, com.user_id,
                (SELECT COUNT(*) FROM proconnect.likes WHERE post_id = pos.id) as total_likes,
                (SELECT COUNT(*) FROM proconnect.comments WHERE post_id = pos.id) as total_comments,
                (SELECT COUNT(*) FROM proconnect.likes WHERE post_id = pos.id AND user_id = :userId) as user_liked
            FROM proconnect.user as us
            LEFT JOIN proconnect.profile_photos as pr ON us.id = pr.user_id
            LEFT JOIN proconnect.posts as pos ON us.id = pos.user_id
            LEFT JOIN proconnect.comments as com ON pos.id = com.post_id
            WHERE us.id = :userId
            ORDER BY pos.date DESC;
                        """, nativeQuery = true)
    List<Object[]> findUsersWithProfilesAndPosts(@Param("userId") Long userId);
}
