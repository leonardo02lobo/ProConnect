package Proconnect.backProconnect.controller;

import Proconnect.backProconnect.model.FriendRequestCreate;
import Proconnect.backProconnect.model.FriendRequestDto;
import Proconnect.backProconnect.model.PageResponse;
import Proconnect.backProconnect.security.CurrentUser;
import Proconnect.backProconnect.service.FriendService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Solicitudes de amistad y amistades del usuario de la sesión. */
@RestController
@RequestMapping("/api/friends")
public class FriendController {

    private final FriendService friendService;

    public FriendController(FriendService friendService) {
        this.friendService = friendService;
    }

    /** Envía una solicitud al usuario {@code userId} del body. */
    @PostMapping("/requests")
    public ResponseEntity<FriendRequestDto> send(@RequestBody @Valid FriendRequestCreate request) {
        FriendRequestDto created = friendService.sendRequest(CurrentUser.id(), request.userId());
        return ResponseEntity.created(URI.create("/api/friends/requests/" + created.id()))
                .body(created);
    }

    /** Solicitudes pendientes recibidas (cada una con el {@code id} para aceptar o rechazar). */
    @GetMapping("/requests")
    public PageResponse<FriendRequestDto> pending(
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size) {
        return friendService.pendingReceivedBy(CurrentUser.id(), page, size);
    }

    @PostMapping("/requests/{id}/accept")
    public FriendRequestDto accept(@PathVariable Long id) {
        return friendService.accept(id);
    }

    @PostMapping("/requests/{id}/reject")
    public FriendRequestDto reject(@PathVariable Long id) {
        return friendService.reject(id);
    }

    /** El emisor cancela su solicitud pendiente. */
    @DeleteMapping("/requests/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable Long id) {
        friendService.cancel(id);
    }

    /** Elimina la amistad con {userId}. */
    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unfriend(@PathVariable Long userId) {
        friendService.removeFriendship(CurrentUser.id(), userId);
    }
}
