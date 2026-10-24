package Proconnect.backProconnect.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import Proconnect.backProconnect.model.PostsDto;
import Proconnect.backProconnect.service.PostsService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/posts")
public class PostsController {
    
    private final PostsService postsService;

    public PostsController(PostsService postsService){
        this.postsService = postsService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> AddPost(@RequestBody @Valid PostsDto postsDto){
        return ResponseEntity.status(HttpStatus.CREATED).body(postsService.AddPost(postsDto));
    }

    @PutMapping("/edit/{Id}")
    public ResponseEntity<?> EditPostById(@PathVariable Long Id, @RequestBody @Valid PostsDto newPost){
        return ResponseEntity.ok().body(postsService.EditPostById(Id, newPost));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> GetAllPosts(@PathVariable Long userId){
        return ResponseEntity.ok().body(postsService.GetAllPosts(userId));
    }

     @GetMapping
    public ResponseEntity<?> GetAllPost(){
        return ResponseEntity.ok().body(postsService.GetAllPosts());
    }

    @GetMapping("/find/{Id}")
    public ResponseEntity<?> GetPostsById(@PathVariable Long Id){
        return ResponseEntity.ok().body(postsService.GetPostsById(Id));
    }
    @DeleteMapping("/delete/{Id}")
    public ResponseEntity<?> DeletePostById(@PathVariable Long Id){
        boolean isDelete = postsService.DeletePostById(Id);
        if(isDelete){
            return ResponseEntity.ok().body("Post deleted successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Post not found.");
        }
    }
}
