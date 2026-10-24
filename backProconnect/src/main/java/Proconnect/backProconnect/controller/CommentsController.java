package Proconnect.backProconnect.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import Proconnect.backProconnect.model.CommentsDto;
import Proconnect.backProconnect.security.CurrentUser;
import Proconnect.backProconnect.service.CommentsService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/comment")
public class CommentsController {
    
    private final CommentsService commentsService;

    public CommentsController(CommentsService commentsService){
        this.commentsService = commentsService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> AddComments(@RequestBody @Valid CommentsDto comments){
        comments.setUserId(CurrentUser.id());
        return ResponseEntity.ok().body(commentsService.AddComments(comments));
    }

    @DeleteMapping("/delete/{Id}")
    public ResponseEntity<?> DeleteCommentById(@PathVariable Long Id){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(commentsService.DeleteCommentById(Id));
    }

    @GetMapping("/find/{postId}")
    public ResponseEntity<?> findByPostId(@PathVariable Long postId){
        return ResponseEntity.ok().body(commentsService.findByPostId(postId));
    }
}
