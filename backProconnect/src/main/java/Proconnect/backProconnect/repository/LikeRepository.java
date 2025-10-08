package Proconnect.backProconnect.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import Proconnect.backProconnect.Entity.Like;
import Proconnect.backProconnect.model.LikeDto;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long>{
    List<LikeDto> findByPostId(Long postId);
    boolean existsByUserIdAndPostId(Long userId, Long postId);
}