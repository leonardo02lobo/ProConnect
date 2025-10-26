package Proconnect.backProconnect.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import Proconnect.backProconnect.Entity.Posts;

@Repository
public interface PostsRepository extends JpaRepository<Posts, Long>{
    @Query(value = """
            SELECT  
                pos.id, pos.title, pos.content, pos.date, pos.image_url,
                us.id as userId, us.email, us.name, us.role, us.user_name,
                prof.id as profileId,prof.photo_url, prof.wall_photo_url,
                (SELECT COUNT(*) FROM proconnect.likes WHERE post_id = pos.id) as total_likes,
                (SELECT COUNT(*) FROM proconnect.comments WHERE post_id = pos.id) as total_comments,
                (SELECT COUNT(*) FROM proconnect.likes WHERE post_id = pos.id AND user_id = :userId) as user_liked
            FROM proconnect.posts as pos
            JOIN proconnect.user as us ON pos.user_id = us.id
            LEFT JOIN proconnect.profile_photos as prof ON us.id = prof.user_id;
            """, nativeQuery = true)
    List<Object[]> GetAllPosts(@Param("userId") Long userId);

    @Query(value = """
            SELECT  
                pos.id, pos.title, pos.content, pos.date, pos.image_url,
                us.id as userId, us.email, us.name, us.role, us.user_name,
                prof.id as profileId,prof.photo_url, prof.wall_photo_url,
                (SELECT COUNT(*) FROM proconnect.likes WHERE post_id = pos.id) as total_likes,
                (SELECT COUNT(*) FROM proconnect.comments WHERE post_id = pos.id) as total_comments
            FROM proconnect.posts as pos
            JOIN proconnect.user as us ON pos.user_id = us.id
            LEFT JOIN proconnect.profile_photos as prof ON us.id = prof.user_id;
            """, nativeQuery = true)
    List<Object[]> GetAllPosts();
}