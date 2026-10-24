package Proconnect.backProconnect.controller;

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

import Proconnect.backProconnect.model.ProfilePhotosDto;
import Proconnect.backProconnect.model.ProfilePhotosRequest;
import Proconnect.backProconnect.security.CurrentUser;
import Proconnect.backProconnect.service.ProfilePhotosService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/profile-photos")
public class ProfilePhotosController {

    private final ProfilePhotosService profilePhotosService;

    public ProfilePhotosController(ProfilePhotosService profilePhotosService) {
        this.profilePhotosService = profilePhotosService;
    }

    /** Crea (201) o reemplaza (200) las fotos del usuario de la sesión. */
    @PostMapping
    public ResponseEntity<ProfilePhotosDto> upsert(@RequestBody @Valid ProfilePhotosRequest request) {
        ProfilePhotosService.Upsert result = profilePhotosService.upsertOwn(CurrentUser.id(), request);
        if (result.created()) {
            return ResponseEntity.created(URI.create("/api/profile-photos/" + result.photos().id()))
                    .body(result.photos());
        }
        return ResponseEntity.ok(result.photos());
    }

    @GetMapping("/{id}")
    public ProfilePhotosDto get(@PathVariable Long id) {
        return profilePhotosService.get(id);
    }

    /** Actualización parcial: los campos null no cambian. */
    @PutMapping("/{id}")
    public ProfilePhotosDto update(@PathVariable Long id, @RequestBody @Valid ProfilePhotosRequest request) {
        return profilePhotosService.update(id, request);
    }
}
