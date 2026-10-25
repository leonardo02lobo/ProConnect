package Proconnect.backProconnect.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import Proconnect.backProconnect.Entity.Friend;
import Proconnect.backProconnect.model.FriendDto;
import Proconnect.backProconnect.security.CurrentUser;
import Proconnect.backProconnect.service.FriendService;

@RestController
@RequestMapping("/api/friend")
public class FriendController {

    private final FriendService friendService;

    public FriendController(FriendService friendService) {
        this.friendService = friendService;
    }

    @PostMapping("/create")
    public ResponseEntity<String> sendFriendRequest(
            @RequestParam(required = false) Long userIdFollow,
            @RequestParam Long userIdBeFollowed) {
        userIdFollow = CurrentUser.id(); // el seguidor siempre es el usuario autenticado

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
            @RequestParam(required = false) Long userIdFollow,
            @RequestParam Long userIdBeFollowed) {
        userIdFollow = CurrentUser.id(); // el seguidor siempre es el usuario autenticado

        String result = friendService.deleteFriendShip(userIdFollow, userIdBeFollowed);
        if (result.toLowerCase().contains("error")) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/cancel")
    public ResponseEntity<String> cancelFriendRequest(
            @RequestParam(required = false) Long userIdFollow,
            @RequestParam Long userIdBeFollowed) {
        userIdFollow = CurrentUser.id(); // el seguidor siempre es el usuario autenticado

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
    public ResponseEntity<List<Friend>> getFriends(@PathVariable Long userId) {
        List<Friend> friends = friendService.getFriends(userId);
        if (friends.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(friends);
    }

    @GetMapping("/find")
    public ResponseEntity<Optional<Friend>> findFriendship(
            @RequestParam Long userId,
            @RequestParam Long otherUserId) {

        Optional<Friend> friendship = friendService.findFriendship(userId, otherUserId);
        if (friendship.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(friendship);
    }
}
