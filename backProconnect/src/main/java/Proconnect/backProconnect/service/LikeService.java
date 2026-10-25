package Proconnect.backProconnect.service;

import Proconnect.backProconnect.Entity.Like;
import Proconnect.backProconnect.exception.ConflictException;
import Proconnect.backProconnect.exception.NotFoundException;
import Proconnect.backProconnect.model.LikeDto;
import Proconnect.backProconnect.model.PageResponse;
import Proconnect.backProconnect.repository.LikeRepository;
import Proconnect.backProconnect.repository.PostsRepository;
import java.time.Instant;
import org.springframework.dao.DataIntegrityViolationException;
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
    public PageResponse<LikeDto> listByPost(Long postId, Integer page, Integer size) {
        assertPostExists(postId);
        return PageResponse.of(
                likeRepository.findByPostId(postId, Paging.page(page, size)), LikeService::toDto);
    }

    /**
     * Like del usuario de la sesión: 404 si el post no existe, 409 si ya le dio like.
     *
     * <p>Orden fijo de locks (post → like) para no provocar deadlocks entre likes concurrentes: el
     * UPDATE del contador bloquea la fila del post y confirma que existe; después el INSERT. Si el
     * UNIQUE(user_id, post_id) rechaza el INSERT (doble like, también concurrente), la transacción
     * entera se revierte —contador incluido— y se responde 409. 2 sentencias.
     */
    @Transactional
    public LikeDto like(Long postId, Long userId) {
        if (postId == null || postId <= 0 || postsRepository.addLikes(postId, 1) == 0) {
            throw new NotFoundException("Post no encontrado");
        }
        try {
            return toDto(
                    likeRepository.saveAndFlush(new Like(null, userId, postId, Instant.now())));
        } catch (DataIntegrityViolationException duplicate) {
            throw new ConflictException("Ya diste like a este post");
        }
    }

    /**
     * Quita el like del usuario de la sesión: 404 si el post no existe o no tenía like. Mismo orden
     * de locks que {@link #like}; si no había like, el rollback deshace el decremento.
     */
    @Transactional
    public void unlike(Long postId, Long userId) {
        if (postId == null || postId <= 0 || postsRepository.addLikes(postId, -1) == 0) {
            throw new NotFoundException("Post no encontrado");
        }
        if (likeRepository.deleteOne(userId, postId) == 0) {
            throw new NotFoundException("No habías dado like a este post");
        }
    }

    private void assertPostExists(Long postId) {
        if (postId == null || postId <= 0 || !postsRepository.existsById(postId)) {
            throw new NotFoundException("Post no encontrado");
        }
    }

    private static LikeDto toDto(Like like) {
        return new LikeDto(like.getId(), like.getUserId(), like.getPostId(), like.getCreatedAt());
    }
}
