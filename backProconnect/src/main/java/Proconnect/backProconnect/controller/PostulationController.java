package Proconnect.backProconnect.controller;

import Proconnect.backProconnect.model.PageResponse;
import Proconnect.backProconnect.model.PostulationDto;
import Proconnect.backProconnect.model.PostulationRequest;
import Proconnect.backProconnect.security.CurrentUser;
import Proconnect.backProconnect.service.PostulationService;
import jakarta.validation.Valid;
import java.net.URI;
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

@RestController
@RequestMapping("/api/postulations")
public class PostulationController {

    private final PostulationService postulationService;

    public PostulationController(PostulationService postulationService) {
        this.postulationService = postulationService;
    }

    @PostMapping
    public ResponseEntity<PostulationDto> create(@RequestBody @Valid PostulationRequest request) {
        PostulationDto created = postulationService.create(CurrentUser.id(), request);
        return ResponseEntity.created(URI.create("/api/postulations/" + created.id()))
                .body(created);
    }

    /** Las propias y las recibidas en empleos de mis empresas (ADMIN: todas). */
    @GetMapping
    public PageResponse<PostulationDto> list(
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size) {
        return postulationService.listVisible(page, size);
    }

    @GetMapping("/{id}")
    public PostulationDto get(@PathVariable Long id) {
        return postulationService.get(id);
    }

    @PutMapping("/{id}")
    public PostulationDto update(
            @PathVariable Long id, @RequestBody @Valid PostulationRequest request) {
        return postulationService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        postulationService.delete(id);
    }

    /** Acepta (dueño de la empresa o ADMIN) y notifica al postulante por correo. Sin body. */
    @PostMapping("/{id}/accept")
    public PostulationDto accept(@PathVariable Long id) {
        return postulationService.accept(id);
    }

    /** Rechaza (dueño de la empresa o ADMIN) y notifica al postulante por correo. Sin body. */
    @PostMapping("/{id}/reject")
    public PostulationDto reject(@PathVariable Long id) {
        return postulationService.reject(id);
    }
}
