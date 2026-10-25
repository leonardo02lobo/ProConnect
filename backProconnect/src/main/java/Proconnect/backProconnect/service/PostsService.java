package Proconnect.backProconnect.service;

import Proconnect.backProconnect.Entity.Posts;
import Proconnect.backProconnect.exception.NotFoundException;
import Proconnect.backProconnect.model.CommentDto;
import Proconnect.backProconnect.model.CursorPage;
import Proconnect.backProconnect.model.FeedPostDto;
import Proconnect.backProconnect.model.PostDto;
import Proconnect.backProconnect.model.PostRequest;
import Proconnect.backProconnect.repository.LikeRepository;
import Proconnect.backProconnect.repository.PostsRepository;
import Proconnect.backProconnect.repository.projection.FeedPostRow;
import Proconnect.backProconnect.security.OwnershipGuard;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostsService {

    private final PostsRepository postsRepository;
    private final LikeRepository likeRepository;
    private final CommentsService commentsService;
    private final UserViews userViews;
    private final OwnershipGuard guard;

    public PostsService(
            PostsRepository postsRepository,
            LikeRepository likeRepository,
            CommentsService commentsService,
            UserViews userViews,
            OwnershipGuard guard) {
        this.postsRepository = postsRepository;
        this.likeRepository = likeRepository;
        this.commentsService = commentsService;
        this.userViews = userViews;
        this.guard = guard;
    }

    /**
     * Feed global cronológico paginado por cursor, en exactamente 3 sentencias por página (design
     * D4): Q1 posts + autor + contadores, Q2 likes del viewer en esa página, Q3 preview de
     * comentarios de toda la página. Una página vacía cuesta solo Q1.
     */
    @Transactional(readOnly = true)
    public CursorPage<FeedPostDto> getFeed(Long viewerId, String cursor, Integer limit) {
        int lim = Paging.cursorLimit(limit);
        Limit fetch = Limit.of(lim + 1);
        List<FeedPostRow> rows =
                Cursor.parse(cursor)
                        .map(c -> postsRepository.feedAfter(c.createdAt(), c.id(), fetch))
                        .orElseGet(() -> postsRepository.feedFirstPage(fetch));
        return assemble(rows, lim, viewerId);
    }

    /** Posts de un autor (perfil), misma forma y coste que el feed. El autor debe existir. */
    @Transactional(readOnly = true)
    public CursorPage<FeedPostDto> getPostsByAuthor(
            Long authorId, Long viewerId, String cursor, Integer limit) {
        int lim = Paging.cursorLimit(limit);
        Limit fetch = Limit.of(lim + 1);
        List<FeedPostRow> rows =
                Cursor.parse(cursor)
                        .map(
                                c ->
                                        postsRepository.byAuthorAfter(
                                                authorId, c.createdAt(), c.id(), fetch))
                        .orElseGet(() -> postsRepository.byAuthorFirstPage(authorId, fetch));
        return assemble(rows, lim, viewerId);
    }

    @Transactional(readOnly = true)
    public PostDto getPost(Long id) {
        return toDto(findOr404(id));
    }

    /** El autor sale de la sesión (lo pasa el controller), nunca del body. */
    @Transactional
    public PostDto createPost(Long authorId, PostRequest request) {
        Posts post =
                new Posts(
                        null,
                        authorId,
                        request.content().trim(),
                        request.title().trim(),
                        blankToNull(request.imageUrl()),
                        Instant.now());
        return toDto(postsRepository.save(post));
    }

    /** Solo el autor o un ADMIN editan (403); 404 si no existe. */
    @Transactional
    public PostDto updatePost(Long id, PostRequest request) {
        Posts post = findOr404(id);
        guard.assertOwnerOrAdmin(post.getUserId());
        post.setTitle(request.title().trim());
        post.setContent(request.content().trim());
        post.setImageUrl(blankToNull(request.imageUrl()));
        return toDto(postsRepository.save(post));
    }

    /**
     * Solo el autor o un ADMIN (moderación) borran (403); 404 si no existe. Sus likes y comentarios
     * caen por ON DELETE CASCADE (V2).
     */
    @Transactional
    public void deletePost(Long id) {
        Posts post = findOr404(id);
        guard.assertOwnerOrAdmin(post.getUserId());
        postsRepository.delete(post);
    }

    Posts findOr404(Long id) {
        if (id == null || id <= 0) {
            throw new NotFoundException("Post no encontrado");
        }
        return postsRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("Post no encontrado"));
    }

    private CursorPage<FeedPostDto> assemble(List<FeedPostRow> rows, int limit, Long viewerId) {
        boolean hasMore = rows.size() > limit;
        List<FeedPostRow> page = hasMore ? rows.subList(0, limit) : rows;
        if (page.isEmpty()) {
            return new CursorPage<>(List.of(), null);
        }
        List<Long> ids = page.stream().map(FeedPostRow::id).toList();
        Set<Long> liked =
                viewerId == null
                        ? Set.of()
                        : new HashSet<>(likeRepository.likedAmong(viewerId, ids));
        Map<Long, List<CommentDto>> previews = commentsService.previewsOf(ids);
        List<FeedPostDto> items =
                page.stream()
                        .map(
                                row ->
                                        toFeedPost(
                                                row,
                                                liked.contains(row.id()),
                                                previews.getOrDefault(row.id(), List.of())))
                        .toList();
        FeedPostRow last = page.get(page.size() - 1);
        String next = hasMore ? Cursor.of(last.createdAt(), last.id()).encode() : null;
        return new CursorPage<>(items, next);
    }

    private FeedPostDto toFeedPost(FeedPostRow row, boolean liked, List<CommentDto> preview) {
        return new FeedPostDto(
                row.id(),
                row.authorId(),
                userViews.publicView(
                        row.authorId(),
                        row.authorName(),
                        row.authorUserName(),
                        row.authorEmail(),
                        row.authorRole() == null ? null : row.authorRole().name(),
                        row.authorAvatarUrl(),
                        row.authorCoverUrl()),
                row.title(),
                row.content(),
                row.imageUrl(),
                row.createdAt(),
                row.likeCount(),
                row.commentCount(),
                liked,
                preview);
    }

    private static PostDto toDto(Posts post) {
        return new PostDto(
                post.getId(),
                post.getUserId(),
                post.getTitle(),
                post.getContent(),
                post.getImageUrl(),
                post.getCreatedAt());
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
