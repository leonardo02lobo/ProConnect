package Proconnect.backProconnect.controller;

import Proconnect.backProconnect.model.CursorPage;
import Proconnect.backProconnect.model.FeedPostDto;
import Proconnect.backProconnect.model.FriendRequestDto;
import Proconnect.backProconnect.model.PageResponse;
import Proconnect.backProconnect.model.UserMediaRequest;
import Proconnect.backProconnect.model.UserProfileDto;
import Proconnect.backProconnect.model.UserPublicDto;
import Proconnect.backProconnect.model.UserUpdateRequest;
import Proconnect.backProconnect.security.CurrentUser;
import Proconnect.backProconnect.service.FriendService;
import Proconnect.backProconnect.service.UserService;
import jakarta.validation.Valid;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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

    /** Usuarios, o los que coinciden con {@code q} (nombre o userName), paginados (máx. 100). */
    @GetMapping
    public PageResponse<UserPublicDto> list(
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size) {
        return userService.listUsers(Optional.ofNullable(query), page, size);
    }

    @GetMapping("/{id}")
    public UserPublicDto get(@PathVariable Long id) {
        return userService.getUser(id);
    }

    @PutMapping("/{id}")
    public UserPublicDto update(
            @PathVariable Long id, @RequestBody @Valid UserUpdateRequest request) {
        return userService.updateUser(id, request);
    }

    /**
     * Avatar y portada (parcial: null = sin cambio, "" = borrar). Solo el propio usuario o ADMIN.
     */
    @PutMapping("/{id}/media")
    public UserPublicDto updateMedia(
            @PathVariable Long id, @RequestBody @Valid UserMediaRequest request) {
        return userService.updateMedia(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    /**
     * Perfil con la primera página de sus posts ({@code nextCursor} → {@code /posts?cursor=});
     * {@code userLiked} es del usuario de la sesión.
     */
    @GetMapping("/{id}/profile")
    public UserProfileDto profile(@PathVariable Long id) {
        return userService.getProfile(id, CurrentUser.id());
    }

    /** Páginas siguientes de los posts del usuario (cursor del perfil o de la página anterior). */
    @GetMapping("/{id}/posts")
    public CursorPage<FeedPostDto> posts(
            @PathVariable Long id,
            @RequestParam(name = "cursor", required = false) String cursor,
            @RequestParam(name = "limit", required = false) Integer limit) {
        return userService.getPostsOf(id, CurrentUser.id(), cursor, limit);
    }

    /** Amistades aceptadas del usuario. */
    @GetMapping("/{id}/friends")
    public PageResponse<FriendRequestDto> friends(
            @PathVariable Long id,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size) {
        return friendService.friendsOf(id, page, size);
    }

    /** Relación entre el usuario de la sesión y {id} (404 si no hay ninguna). */
    @GetMapping("/{id}/friendship")
    public FriendRequestDto friendship(@PathVariable Long id) {
        return friendService.relationBetween(CurrentUser.id(), id);
    }
}
