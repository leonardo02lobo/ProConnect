package Proconnect.backProconnect.service;

import Proconnect.backProconnect.Entity.Posts;
import Proconnect.backProconnect.exception.NotFoundException;
import Proconnect.backProconnect.model.FeedPostDto;
import Proconnect.backProconnect.model.PostDto;
import Proconnect.backProconnect.model.PostRequest;
import Proconnect.backProconnect.repository.CommentsRepository;
import Proconnect.backProconnect.repository.LikeRepository;
import Proconnect.backProconnect.repository.PostsRepository;
import Proconnect.backProconnect.repository.projection.FeedRow;
import Proconnect.backProconnect.security.OwnershipGuard;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostsService {

    private final PostsRepository postsRepository;
    private final CommentsRepository commentsRepository;
    private final LikeRepository likeRepository;
    private final CommentsService commentsService;
    private final UserViews userViews;
    private final OwnershipGuard guard;

    public PostsService(
            PostsRepository postsRepository,
            CommentsRepository commentsRepository,
            LikeRepository likeRepository,
            CommentsService commentsService,
            UserViews userViews,
            OwnershipGuard guard) {
        this.postsRepository = postsRepository;
        this.commentsRepository = commentsRepository;
        this.likeRepository = likeRepository;
        this.commentsService = commentsService;
        this.userViews = userViews;
        this.guard = guard;
    }

    /**
     * Feed completo, del más nuevo al más antiguo. {@code userLiked} es del viewer (false si no hay
     * viewer).
     */
    @Transactional(readOnly = true)
    public List<FeedPostDto> getFeed(Optional<Long> viewerId) {
        return toFeed(postsRepository.findFeed(viewerId.orElse(null), null));
    }

    /** Posts de un autor (perfil) vistos por el viewer. */
    @Transactional(readOnly = true)
    public List<FeedPostDto> getPostsByAuthor(Long authorId, Optional<Long> viewerId) {
        return toFeed(postsRepository.findFeed(viewerId.orElse(null), authorId));
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
                        new Date());
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
     * Solo el autor o un ADMIN (moderación) borran (403); 404 si no existe. Arrastra sus likes y
     * comentarios.
     */
    @Transactional
    public void deletePost(Long id) {
        Posts post = findOr404(id);
        guard.assertOwnerOrAdmin(post.getUserId());
        likeRepository.deleteByPostId(post.getId());
        commentsRepository.deleteByPostId(post.getId());
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

    private List<FeedPostDto> toFeed(List<FeedRow> rows) {
        return rows.stream().map(this::toFeedPost).toList();
    }

    private FeedPostDto toFeedPost(FeedRow row) {
        return new FeedPostDto(
                row.getId(),
                row.getUserId(),
                userViews.publicView(
                        row.getUserId(),
                        row.getUserName(),
                        row.getUserUserName(),
                        row.getUserEmail(),
                        row.getUserRole(),
                        row.getPhotoUrl(),
                        row.getWallPhotoUrl()),
                row.getTitle(),
                row.getContent(),
                row.getImageUrl(),
                row.getDate(),
                count(row.getTotalLikes()),
                count(row.getTotalComments()),
                count(row.getUserLiked()) > 0,
                commentsService.commentsOf(row.getId()));
    }

    private static PostDto toDto(Posts post) {
        return new PostDto(
                post.getId(),
                post.getUserId(),
                post.getTitle(),
                post.getContent(),
                post.getImageUrl(),
                post.getDate());
    }

    private static long count(Long value) {
        return value == null ? 0 : value;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
