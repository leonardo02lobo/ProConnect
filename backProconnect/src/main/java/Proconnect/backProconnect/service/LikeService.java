package Proconnect.backProconnect.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import Proconnect.backProconnect.Entity.Like;
import Proconnect.backProconnect.model.LikeDto;
import Proconnect.backProconnect.repository.LikeRepository;

@Service
public class LikeService {

    private final LikeRepository likeRepository;

    public LikeService(LikeRepository likeRepository) {
        this.likeRepository = likeRepository;
    }

    public String addLike(LikeDto likeDto) {
        try {
            if (likeDto == null) {
                return "Like cannot be null";
            }

            boolean exists = likeRepository.existsByUserIdAndPostId(likeDto.getUserId(), likeDto.getPostId());
            if (exists) {
                return "User already liked this post";
            }
            Like like = new Like(null, likeDto.getUserId(), likeDto.getPostId(), likeDto.getDate());
            likeRepository.save(like);

            return "Like added successfully";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    public String removeLikeByUserAndPost(Long userId, Long postId) {
    try {
        Optional<Like> like = likeRepository.findByUserIdAndPostId(userId, postId);
        if (like.isEmpty()) {
            return "Like not found";
        }
        likeRepository.delete(like.get());
        return "Like removed successfully";
    } catch (Exception e) {
        return "Error: " + e.getMessage();
    }
}

    public List<LikeDto> getLikesByPost(Long postId) {
        return likeRepository.findByPostId(postId);
    }

    public boolean existsByUserAndPost(Long userId, Long postId) {
        return likeRepository.existsByUserIdAndPostId(userId, postId);
    }
}
