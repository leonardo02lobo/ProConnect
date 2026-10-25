package Proconnect.backProconnect.service;

import Proconnect.backProconnect.Entity.Like;
import Proconnect.backProconnect.exception.ConflictException;
import Proconnect.backProconnect.exception.NotFoundException;
import Proconnect.backProconnect.model.LikeDto;
import Proconnect.backProconnect.repository.LikeRepository;
import Proconnect.backProconnect.repository.PostsRepository;
import java.util.Date;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LikeService {

    private final LikeRepository likeRepository;
    private final PostsRepository postsRepository;

    public LikeService(LikeRepository likeRepository, PostsRepository postsRepository) {
        this.likeRepository = likeRepository;
        this.postsRepository = postsRepository;
    }

    @Transactional(readOnly = true)
    public List<LikeDto> listByPost(Long postId) {
        assertPostExists(postId);
        return likeRepository.findByPostIdOrderByIdAsc(postId).stream()
                .map(LikeService::toDto)
                .toList();
    }

    /** Like del usuario de la sesión: 404 si el post no existe, 409 si ya le dio like. */
    @Transactional
    public LikeDto like(Long postId, Long userId) {
        assertPostExists(postId);
        if (likeRepository.existsByUserIdAndPostId(userId, postId)) {
            throw new ConflictException("Ya diste like a este post");
        }
        return toDto(likeRepository.save(new Like(null, userId, postId, new Date())));
    }

    /** Quita el like del usuario de la sesión: 404 si el post no existe o no tenía like. */
    @Transactional
    public void unlike(Long postId, Long userId) {
        assertPostExists(postId);
        Like like =
                likeRepository
                        .findByUserIdAndPostId(userId, postId)
                        .orElseThrow(
                                () -> new NotFoundException("No habías dado like a este post"));
        likeRepository.delete(like);
    }

    private void assertPostExists(Long postId) {
        if (postId == null || postId <= 0 || !postsRepository.existsById(postId)) {
            throw new NotFoundException("Post no encontrado");
        }
    }

    private static LikeDto toDto(Like like) {
        return new LikeDto(like.getId(), like.getUserId(), like.getPostId(), like.getDate());
    }
}
