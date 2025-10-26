package Proconnect.backProconnect.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import Proconnect.backProconnect.Entity.Comments;

@Repository
public interface CommentsRepository extends JpaRepository<Comments,Long>{
    List<Comments> findByPostId(Long postId);

    @Query(value = """
            SELECT 
                us.id, us.email, us.name, us.role, us.user_name,
                prof.id, prof.photo_url, prof.wall_photo_url
            FROM proconnect.comments as com
            JOIN proconnect.user as us ON com.user_id = us.id
            LEFT JOIN proconnect.profile_photos as prof ON us.id = prof.user_id
            WHERE com.id = :comId;
            """, nativeQuery = true)
    List<Object[]> findUserWithComment(@Param("comId") Long comId);
}
