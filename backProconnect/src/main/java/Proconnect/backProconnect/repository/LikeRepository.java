package Proconnect.backProconnect.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import Proconnect.backProconnect.Entity.Like;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    List<Like> findByPostIdOrderByIdAsc(Long postId);

    boolean existsByUserIdAndPostId(Long userId, Long postId);

    Optional<Like> findByUserIdAndPostId(Long userId, Long postId);

    void deleteByPostId(Long postId);
}
