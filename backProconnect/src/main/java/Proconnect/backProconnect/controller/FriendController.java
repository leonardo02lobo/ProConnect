package Proconnect.backProconnect.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import Proconnect.backProconnect.model.FriendDto;
import Proconnect.backProconnect.service.FriendService;

@RestController
@RequestMapping("/api/friend")
@CrossOrigin(origins = "*") 
public class FriendController {

    private final FriendService friendService;

    public FriendController(FriendService friendService) {
        this.friendService = friendService;
    }

    @PostMapping("/create")
    public ResponseEntity<String> sendFriendRequest(
            @RequestParam Long userIdFollow,
            @RequestParam Long userIdBeFollowed) {

        String result = friendService.sendFriendRequest(userIdFollow, userIdBeFollowed);
        if (result.toLowerCase().contains("error")) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @PutMapping("/accept/{userIdBeFollowed}")
    public ResponseEntity<String> acceptFriendRequest(@PathVariable Long userIdBeFollowed) {
        String result = friendService.AcceptFriendUserIdBeFollowed(userIdBeFollowed);
        if (result.toLowerCase().contains("error")) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @PutMapping("/reject/{userIdBeFollowed}")
    public ResponseEntity<String> rejectFriendRequest(@PathVariable Long userIdBeFollowed) {
        String result = friendService.rejectFriendRequest(userIdBeFollowed);
        if (result.toLowerCase().contains("error")) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteFriendship(
            @RequestParam Long userIdFollow,
            @RequestParam Long userIdBeFollowed) {

        String result = friendService.deleteFriendShip(userIdFollow, userIdBeFollowed);
        if (result.toLowerCase().contains("error")) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/cancel")
    public ResponseEntity<String> cancelFriendRequest(
            @RequestParam Long userIdFollow,
            @RequestParam Long userIdBeFollowed) {

        String result = friendService.cancelFriendUserIdBeFollowed(userIdFollow, userIdBeFollowed);
        if (result.toLowerCase().contains("error")) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/pending/{userId}")
    public ResponseEntity<List<FriendDto>> getPendingRequests(@PathVariable Long userId) {
        List<FriendDto> pending = friendService.getPendingRequests(userId);
        if (pending.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(pending);
    }

    @GetMapping("/all/{userId}")
    public ResponseEntity<List<FriendDto>> getFriends(@PathVariable Long userId) {
        List<FriendDto> friends = friendService.getFriends(userId);
        if (friends.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(friends);
    }

    @GetMapping("/find")
    public ResponseEntity<Optional<FriendDto>> findFriendship(
            @RequestParam Long userId,
            @RequestParam Long otherUserId) {

        Optional<FriendDto> friendship = friendService.findFriendship(userId, otherUserId);
        if (friendship.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(friendship);
    }
}
