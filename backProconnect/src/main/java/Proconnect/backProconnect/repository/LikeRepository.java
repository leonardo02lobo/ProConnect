package Proconnect.backProconnect.repository;

import Proconnect.backProconnect.Entity.Like;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

    Slice<Like> findByPostId(Long postId, Pageable pageable);

    boolean existsByUserIdAndPostId(Long userId, Long postId);

    /**
     * De una página de posts, cuáles tienen like del viewer: UNA query sobre {@code
     * uk_likes_user_post (user_id, post_id)} (design D4, Q2).
     */
    @Query("SELECT l.postId FROM Like l WHERE l.userId = :viewerId AND l.postId IN :postIds")
    List<Long> likedAmong(
            @Param("viewerId") Long viewerId, @Param("postIds") Collection<Long> postIds);

    /** Borra el like del usuario en un solo DELETE; devuelve las filas borradas (0 o 1). */
    @Modifying(flushAutomatically = true)
    @Query("DELETE FROM Like l WHERE l.userId = :userId AND l.postId = :postId")
    int deleteOne(@Param("userId") Long userId, @Param("postId") Long postId);
}
