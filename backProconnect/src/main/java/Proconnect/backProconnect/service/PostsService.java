package Proconnect.backProconnect.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import Proconnect.backProconnect.Entity.Comments;
import Proconnect.backProconnect.Entity.Posts;
import Proconnect.backProconnect.model.CommentsDto;
import Proconnect.backProconnect.model.PostsDto;
import Proconnect.backProconnect.model.PostsResponse;
import Proconnect.backProconnect.model.UserPublicDto;
import Proconnect.backProconnect.repository.CommentsRepository;
import Proconnect.backProconnect.repository.PostsRepository;
import Proconnect.backProconnect.exception.ResourceNotFoundException;
import Proconnect.backProconnect.security.OwnershipGuard;

@Service
public class PostsService {
    private final PostsRepository postsRepository;
    private final CommentsRepository commentsRepository;
    private final OwnershipGuard guard;

    public PostsService(PostsRepository postsRepository, CommentsRepository commentsRepository,
            OwnershipGuard guard) {
        this.postsRepository = postsRepository;
        this.commentsRepository = commentsRepository;
        this.guard = guard;
    }

    public String AddPost(PostsDto postsDto) {
        try {
            if (postsDto == null) {
                return "Posts cannot be null";
            }
            Posts posts = new Posts(null, postsDto.getUserId(), postsDto.getContent(), postsDto.getTitle(),
                    postsDto.getImageUrl(), new java.util.Date());
            postsRepository.save(posts);
            return "Posts Add Successufly";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    /** Solo el autor o un ADMIN editan (403); 404 si no existe. */
    public String EditPostById(Long Id, PostsDto newPost) {
        if (newPost == null) {
            return "Post cannot be null";
        }
        Posts post = findOr404(Id);
        guard.assertOwnerOrAdmin(post.getUserId());
        post.setContent(newPost.getContent());
        post.setImageUrl(newPost.getImageUrl());
        post.setTitle(newPost.getTitle());
        postsRepository.save(post);
        return "Edited Post";
    }

    /** El email solo se expone al propio usuario o a un ADMIN. */
    private String privateEmail(Long userId, String email) {
        return guard.canSeePrivateDataOf(userId) ? email : null;
    }

    private Posts findOr404(Long id) {
        if (id == null || id <= 0) {
            throw new ResourceNotFoundException();
        }
        return postsRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
    }

    public List<PostsResponse> GetAllPosts(Long userId) {
        List<Object[]> posts = postsRepository.GetAllPosts(userId);

        if (posts.isEmpty())
            return null;

        List<PostsResponse> postsResponses = new ArrayList<>();
        for (Object[] row : posts) {

            List<Comments> commentEntities = commentsRepository.findByPostId(((Number) row[0]).longValue());
            List<CommentsDto> commentDtos = commentEntities.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());

            PostsResponse post = new PostsResponse(
                    ((Number) row[0]).longValue(),
                    ((Number) row[5]).longValue(),
                    new UserPublicDto(
                            ((Number) row[5]).longValue(),
                            (String) row[7],
                            (String) row[9],
                            privateEmail(((Number) row[5]).longValue(), (String) row[6]),
                            (String) row[8],
                            (String) row[11],
                            (String) row[12]),
                    (String) row[2],
                    (String) row[1],
                    (String) row[4],
                    (java.util.Date) row[3],
                    ((Number) row[13]).longValue(),
                    ((Number) row[14]).longValue(),
                    ((Number) row[15]).longValue() > 0,
                    commentDtos);
            postsResponses.add(post);
        }
        return postsResponses;
    }

    public List<PostsResponse> GetAllPosts() {
        List<Object[]> posts = postsRepository.GetAllPosts();

        if (posts.isEmpty())
            return null;

        List<PostsResponse> postsResponses = new ArrayList<>();
        for (Object[] row : posts) {

            List<Comments> commentEntities = commentsRepository.findByPostId(((Number) row[0]).longValue());
            List<CommentsDto> commentDtos = commentEntities.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());

            PostsResponse post = new PostsResponse(
                    ((Number) row[0]).longValue(),
                    ((Number) row[5]).longValue(),
                    new UserPublicDto(
                            ((Number) row[5]).longValue(),
                            (String) row[7],
                            (String) row[9],
                            privateEmail(((Number) row[5]).longValue(), (String) row[6]),
                            (String) row[8],
                            (String) row[11],
                            (String) row[12]),
                    (String) row[2],
                    (String) row[1],
                    (String) row[4],
                    (java.util.Date) row[3],
                    ((Number) row[13]).longValue(),
                    ((Number) row[14]).longValue(),
                    false,
                    commentDtos);
            postsResponses.add(post);
        }
        return postsResponses;
    }

    public Posts GetPostsById(Long Id) {
        try {
            if (Long.valueOf(Id) == null || Id == 0) {
                return null;
            }
            Optional<Posts> post = postsRepository.findById(Id);
            return post.orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    /** Solo el autor o un ADMIN (moderación) borran (403); 404 si no existe. */
    public void DeletePostById(Long Id) {
        Posts post = findOr404(Id);
        guard.assertOwnerOrAdmin(post.getUserId());
        postsRepository.delete(post);
    }

    private CommentsDto convertToDto(Comments comment) {
        List<Object[]> userList = commentsRepository.findUserWithComment(comment.getId());
        UserPublicDto author = null;
        if (!userList.isEmpty()) {
            Object[] user = userList.get(0);
            author = new UserPublicDto(
                    ((Number) user[0]).longValue(),
                    (String) user[2],
                    (String) user[4],
                    privateEmail(((Number) user[0]).longValue(), (String) user[1]),
                    (String) user[3],
                    (String) user[6],
                    (String) user[7]);
        }

        return new CommentsDto(
                comment.getId(),
                comment.getPostId(),
                comment.getUserId(),
                comment.getContent(),
                comment.getDate(),
                author);
    }
}
