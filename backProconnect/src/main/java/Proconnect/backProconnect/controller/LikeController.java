package Proconnect.backProconnect.controller;

import java.util.Map;

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

    @DeleteMapping("/delete")
    public ResponseEntity<?> removeLike(@RequestBody Map<String, Long> request) {
        Long userId = request.get("userId");
        Long postId = request.get("postId");
        
        try {
            String result = likeService.removeLikeByUserAndPost(userId, postId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Error removing like: " + e.getMessage());
        }
    }

    @GetMapping("/list/{postId}")
    public ResponseEntity<?> getLikesByPOst(@PathVariable Long postId){
        return ResponseEntity.ok().body(likeService.getLikesByPost(postId));
    }
}
