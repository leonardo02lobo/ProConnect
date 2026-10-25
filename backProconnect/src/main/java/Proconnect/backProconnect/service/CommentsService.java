package Proconnect.backProconnect.service;

import Proconnect.backProconnect.Entity.Comments;
import Proconnect.backProconnect.exception.NotFoundException;
import Proconnect.backProconnect.model.CommentDto;
import Proconnect.backProconnect.model.CommentRequest;
import Proconnect.backProconnect.repository.CommentsRepository;
import Proconnect.backProconnect.repository.PostsRepository;
import Proconnect.backProconnect.repository.projection.CommentRow;
import Proconnect.backProconnect.security.OwnershipGuard;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentsService {

    private final CommentsRepository commentsRepository;
    private final PostsRepository postsRepository;
    private final UserViews userViews;
    private final OwnershipGuard guard;

    public CommentsService(
            CommentsRepository commentsRepository,
            PostsRepository postsRepository,
            UserViews userViews,
            OwnershipGuard guard) {
        this.commentsRepository = commentsRepository;
        this.postsRepository = postsRepository;
        this.userViews = userViews;
        this.guard = guard;
    }

    /** Comentarios de un post existente (404 si el post no existe). */
    @Transactional(readOnly = true)
    public List<CommentDto> listByPost(Long postId) {
        assertPostExists(postId);
        return commentsOf(postId);
    }

    /** Sin comprobar el post: lo usa el feed, que ya parte de posts existentes. */
    List<CommentDto> commentsOf(Long postId) {
        return commentsRepository.findWithAuthorByPostId(postId).stream().map(this::toDto).toList();
    }

    /** Post y autor salen de la ruta y de la sesión; cualquier id del body se ignora. */
    @Transactional
    public CommentDto create(Long postId, Long authorId, CommentRequest request) {
        assertPostExists(postId);
        Comments saved =
                commentsRepository.save(
                        new Comments(
                                null, postId, authorId, request.content().trim(), Instant.now()));
        return commentsRepository.findWithAuthorById(saved.getId()).stream()
                .findFirst()
                .map(this::toDto)
                .orElseThrow(() -> new NotFoundException("Comentario no encontrado"));
    }

    /** Solo el autor o un ADMIN (moderación) borran (403); 404 si no existe. */
    @Transactional
    public void delete(Long id) {
        if (id == null || id <= 0) {
            throw new NotFoundException("Comentario no encontrado");
        }
        Comments comment =
                commentsRepository
                        .findById(id)
                        .orElseThrow(() -> new NotFoundException("Comentario no encontrado"));
        guard.assertOwnerOrAdmin(comment.getUserId());
        commentsRepository.delete(comment);
    }

    private void assertPostExists(Long postId) {
        if (postId == null || postId <= 0 || !postsRepository.existsById(postId)) {
            throw new NotFoundException("Post no encontrado");
        }
    }

    private CommentDto toDto(CommentRow row) {
        return new CommentDto(
                row.getId(),
                row.getPostId(),
                row.getUserId(),
                row.getContent(),
                row.getDate(),
                userViews.publicView(
                        row.getUserId(),
                        row.getUserName(),
                        row.getUserUserName(),
                        row.getUserEmail(),
                        row.getUserRole(),
                        row.getPhotoUrl(),
                        row.getWallPhotoUrl()));
    }
}
