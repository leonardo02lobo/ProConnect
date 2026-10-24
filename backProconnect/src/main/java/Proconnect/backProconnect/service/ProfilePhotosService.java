package Proconnect.backProconnect.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import Proconnect.backProconnect.Entity.ProfilePhotos;
import Proconnect.backProconnect.exception.NotFoundException;
import Proconnect.backProconnect.model.ProfilePhotosDto;
import Proconnect.backProconnect.model.ProfilePhotosRequest;
import Proconnect.backProconnect.repository.ProfilePhotosRepository;
import Proconnect.backProconnect.security.OwnershipGuard;

@Service
public class ProfilePhotosService {

    private final ProfilePhotosRepository profilePhotosRepository;
    private final OwnershipGuard guard;

    public ProfilePhotosService(ProfilePhotosRepository profilePhotosRepository, OwnershipGuard guard) {
        this.profilePhotosRepository = profilePhotosRepository;
        this.guard = guard;
    }

    /** Resultado de crear-o-reemplazar: {@code created} distingue 201 de 200. */
    public record Upsert(ProfilePhotosDto photos, boolean created) {
    }

    /**
     * Crea o reemplaza las fotos del usuario de la sesión. Nunca se reutiliza un id del body (evita sobrescribir la
     * fila de otro usuario).
     */
    @Transactional
    public Upsert upsertOwn(Long userId, ProfilePhotosRequest request) {
        var existing = profilePhotosRepository.findFirstByUserIdOrderByIdDesc(userId);
        ProfilePhotos photos = existing.orElseGet(() -> new ProfilePhotos(null, userId, null, null));
        photos.setPhotoUrl(request.photoUrl());
        photos.setWallPhotoUrl(request.wallPhotoUrl());
        return new Upsert(toDto(profilePhotosRepository.save(photos)), existing.isEmpty());
    }

    @Transactional(readOnly = true)
    public ProfilePhotosDto get(Long id) {
        return toDto(findOr404(id));
    }

    /** Actualización parcial (null = sin cambio). Solo el propietario o un ADMIN (403); 404 si no existe. */
    @Transactional
    public ProfilePhotosDto update(Long id, ProfilePhotosRequest request) {
        ProfilePhotos photos = findOr404(id);
        guard.assertOwnerOrAdmin(photos.getUserId());
        if (request.photoUrl() != null) {
            photos.setPhotoUrl(request.photoUrl());
        }
        if (request.wallPhotoUrl() != null) {
            photos.setWallPhotoUrl(request.wallPhotoUrl());
        }
        return toDto(profilePhotosRepository.save(photos));
    }

    private ProfilePhotos findOr404(Long id) {
        if (id == null || id <= 0) {
            throw new NotFoundException("Fotos no encontradas");
        }
        return profilePhotosRepository.findById(id).orElseThrow(() -> new NotFoundException("Fotos no encontradas"));
    }

    private static ProfilePhotosDto toDto(ProfilePhotos p) {
        return new ProfilePhotosDto(p.getId(), p.getUserId(), p.getPhotoUrl(), p.getWallPhotoUrl());
    }
}
