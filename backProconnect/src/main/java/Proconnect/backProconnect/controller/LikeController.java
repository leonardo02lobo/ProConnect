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

import Proconnect.backProconnect.model.LikeDto;
import Proconnect.backProconnect.service.LikeService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/like")
public class LikeController {
    
    private final LikeService likeService;

    public LikeController(LikeService likeService){
        this.likeService = likeService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> addLike(@RequestBody @Valid LikeDto likeDto){
        return ResponseEntity.status(HttpStatus.CREATED).body(likeService.addLike(likeDto));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> removeLike(@PathVariable Long id){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(likeService.removeLike(id));
    }

    @GetMapping("/list/{postId}")
    public ResponseEntity<?> getLikesByPOst(@PathVariable Long postId){
        return ResponseEntity.ok().body(likeService.getLikesByPost(postId));
    }
}
