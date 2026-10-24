package Proconnect.backProconnect.controller;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import Proconnect.backProconnect.model.FriendRequestDto;
import Proconnect.backProconnect.model.UserProfileDto;
import Proconnect.backProconnect.model.UserPublicDto;
import Proconnect.backProconnect.model.UserUpdateRequest;
import Proconnect.backProconnect.security.CurrentUser;
import Proconnect.backProconnect.service.FriendService;
import Proconnect.backProconnect.service.UserService;
import jakarta.validation.Valid;

/** Usuarios. El alta es POST /api/auth/register. */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final FriendService friendService;

    public UserController(UserService userService, FriendService friendService) {
        this.userService = userService;
        this.friendService = friendService;
    }

    /** Todos los usuarios, o los que coinciden con {@code q} (nombre o userName). */
    @GetMapping
    public List<UserPublicDto> list(@RequestParam(name = "q", required = false) String query) {
        return userService.listUsers(Optional.ofNullable(query));
    }

    @GetMapping("/{id}")
    public UserPublicDto get(@PathVariable Long id) {
        return userService.getUser(id);
    }

    @PutMapping("/{id}")
    public UserPublicDto update(@PathVariable Long id, @RequestBody @Valid UserUpdateRequest request) {
        return userService.updateUser(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    /** Perfil con sus posts; {@code userLiked} es del usuario de la sesión. */
    @GetMapping("/{id}/profile")
    public UserProfileDto profile(@PathVariable Long id) {
        return userService.getProfile(id, Optional.of(CurrentUser.id()));
    }

    /** Amistades aceptadas del usuario. */
    @GetMapping("/{id}/friends")
    public List<FriendRequestDto> friends(@PathVariable Long id) {
        return friendService.friendsOf(id);
    }

    /** Relación entre el usuario de la sesión y {id} (404 si no hay ninguna). */
    @GetMapping("/{id}/friendship")
    public FriendRequestDto friendship(@PathVariable Long id) {
        return friendService.relationBetween(CurrentUser.id(), id);
    }
}
