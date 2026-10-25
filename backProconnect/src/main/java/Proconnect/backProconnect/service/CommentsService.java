package Proconnect.backProconnect.service;

import Proconnect.backProconnect.Entity.Comments;
import Proconnect.backProconnect.exception.NotFoundException;
import Proconnect.backProconnect.model.CommentDto;
import Proconnect.backProconnect.model.CommentRequest;
import Proconnect.backProconnect.model.CursorPage;
import Proconnect.backProconnect.repository.CommentsRepository;
import Proconnect.backProconnect.repository.PostsRepository;
import Proconnect.backProconnect.repository.projection.CommentRow;
import Proconnect.backProconnect.repository.projection.CommentView;
import Proconnect.backProconnect.security.OwnershipGuard;
import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Limit;
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

    /**
     * Comentarios de un post, del más antiguo al más nuevo, paginados por cursor (404 si el post no
     * existe). 1 sentencia por página; solo una página vacía comprueba además el post.
     */
    @Transactional(readOnly = true)
    public CursorPage<CommentDto> listByPost(Long postId, String cursor, Integer limit) {
        if (postId == null || postId <= 0) {
            throw new NotFoundException("Post no encontrado");
        }
        int lim = Paging.cursorLimit(limit);
        Limit fetch = Limit.of(lim + 1);
        List<CommentView> rows =
                Cursor.parse(cursor)
                        .map(
                                c ->
                                        commentsRepository.pageAfter(
                                                postId, c.createdAt(), c.id(), fetch))
                        .orElseGet(() -> commentsRepository.pageFirst(postId, fetch));
        if (rows.isEmpty() && !postsRepository.existsById(postId)) {
            throw new NotFoundException("Post no encontrado");
        }
        boolean hasMore = rows.size() > lim;
        List<CommentView> page = hasMore ? rows.subList(0, lim) : rows;
        String next = null;
        if (hasMore) {
            CommentView last = page.get(page.size() - 1);
            next = Cursor.of(last.createdAt(), last.id()).encode();
        }
        return new CursorPage<>(page.stream().map(this::toDto).toList(), next);
    }

    /**
     * Los últimos {@link Paging#COMMENTS_PREVIEW} comentarios de cada post de una página del feed,
     * en UNA query (sin comprobar los posts: el feed ya parte de posts existentes).
     */
    Map<Long, List<CommentDto>> previewsOf(Collection<Long> postIds) {
        if (postIds.isEmpty()) {
            return Map.of();
        }
        return commentsRepository.latestPerPost(postIds, Paging.COMMENTS_PREVIEW).stream()
                .map(this::toDto)
                .collect(
                        Collectors.groupingBy(
                                CommentDto::postId, LinkedHashMap::new, Collectors.toList()));
    }

    /**
     * Post y autor salen de la ruta y de la sesión; cualquier id del body se ignora. El contador
     * del post se actualiza en la misma transacción (design D5): primero el UPDATE (bloquea el post
     * y confirma que existe), luego el INSERT.
     */
    @Transactional
    public CommentDto create(Long postId, Long authorId, CommentRequest request) {
        if (postId == null || postId <= 0 || postsRepository.addComments(postId, 1) == 0) {
            throw new NotFoundException("Post no encontrado");
        }
        Comments saved =
                commentsRepository.save(
                        new Comments(
                                null, postId, authorId, request.content().trim(), Instant.now()));
        commentsRepository.flush();
        return commentsRepository
                .findView(saved.getId())
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
        postsRepository.addComments(comment.getPostId(), -1);
        commentsRepository.delete(comment);
    }

    private CommentDto toDto(CommentView c) {
        return new CommentDto(
                c.id(),
                c.postId(),
                c.userId(),
                c.content(),
                c.createdAt(),
                userViews.publicView(
                        c.userId(),
                        c.authorName(),
                        c.authorUserName(),
                        c.authorEmail(),
                        c.authorRole() == null ? null : c.authorRole().name(),
                        c.authorAvatarUrl(),
                        c.authorCoverUrl()));
    }

    private CommentDto toDto(CommentRow c) {
        return new CommentDto(
                c.getId(),
                c.getPostId(),
                c.getUserId(),
                c.getContent(),
                c.getCreatedAt(),
                userViews.publicView(
                        c.getUserId(),
                        c.getAuthorName(),
                        c.getAuthorUserName(),
                        c.getAuthorEmail(),
                        c.getAuthorRole(),
                        c.getAuthorAvatarUrl(),
                        c.getAuthorCoverUrl()));
    }
}
