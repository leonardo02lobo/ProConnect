package Proconnect.backProconnect.controller;

import Proconnect.backProconnect.model.CommentDto;
import Proconnect.backProconnect.model.CommentRequest;
import Proconnect.backProconnect.model.FeedPostDto;
import Proconnect.backProconnect.model.LikeDto;
import Proconnect.backProconnect.model.PostDto;
import Proconnect.backProconnect.model.PostRequest;
import Proconnect.backProconnect.security.CurrentUser;
import Proconnect.backProconnect.service.CommentsService;
import Proconnect.backProconnect.service.LikeService;
import Proconnect.backProconnect.service.PostsService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Posts y sus sub-recursos (likes y comentarios). La autoría siempre sale de la sesión. */
@RestController
@RequestMapping("/api/posts")
public class PostsController {

    private final PostsService postsService;
    private final LikeService likeService;
    private final CommentsService commentsService;

    public PostsController(
            PostsService postsService, LikeService likeService, CommentsService commentsService) {
        this.postsService = postsService;
        this.likeService = likeService;
        this.commentsService = commentsService;
    }

    /** Feed (más nuevo primero). {@code userLiked} es del usuario de la sesión. */
    @GetMapping
    public List<FeedPostDto> feed() {
        return postsService.getFeed(Optional.of(CurrentUser.id()));
    }

    @GetMapping("/{id}")
    public PostDto get(@PathVariable Long id) {
        return postsService.getPost(id);
    }

    @PostMapping
    public ResponseEntity<PostDto> create(@RequestBody @Valid PostRequest request) {
        PostDto created = postsService.createPost(CurrentUser.id(), request);
        return ResponseEntity.created(URI.create("/api/posts/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public PostDto update(@PathVariable Long id, @RequestBody @Valid PostRequest request) {
        return postsService.updatePost(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        postsService.deletePost(id);
    }

    // ---- Likes ----

    @GetMapping("/{postId}/likes")
    public List<LikeDto> likes(@PathVariable Long postId) {
        return likeService.listByPost(postId);
    }

    /** Like del usuario de la sesión (sin body): 201, 409 si ya existía. */
    @PostMapping("/{postId}/likes")
    public ResponseEntity<LikeDto> like(@PathVariable Long postId) {
        LikeDto like = likeService.like(postId, CurrentUser.id());
        return ResponseEntity.created(URI.create("/api/posts/" + postId + "/likes")).body(like);
    }

    /** Quita el like del usuario de la sesión (sin body): 204. */
    @DeleteMapping("/{postId}/likes")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unlike(@PathVariable Long postId) {
        likeService.unlike(postId, CurrentUser.id());
    }

    // ---- Comentarios ----

    @GetMapping("/{postId}/comments")
    public List<CommentDto> comments(@PathVariable Long postId) {
        return commentsService.listByPost(postId);
    }

    @PostMapping("/{postId}/comments")
    public ResponseEntity<CommentDto> comment(
            @PathVariable Long postId, @RequestBody @Valid CommentRequest request) {
        CommentDto created = commentsService.create(postId, CurrentUser.id(), request);
        return ResponseEntity.created(URI.create("/api/comments/" + created.id())).body(created);
    }
}
