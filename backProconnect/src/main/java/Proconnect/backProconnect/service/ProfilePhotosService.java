package Proconnect.backProconnect.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import Proconnect.backProconnect.Entity.ProfilePhotos;
import Proconnect.backProconnect.model.ProfilePhotosDto;
import Proconnect.backProconnect.repository.ProfilePhotosRepository;
import Proconnect.backProconnect.exception.ResourceNotFoundException;
import Proconnect.backProconnect.security.OwnershipGuard;

@Service
public class ProfilePhotosService {
    
    private final ProfilePhotosRepository profilePhotosRepository;
    private final OwnershipGuard guard;

    public ProfilePhotosService(ProfilePhotosRepository profilePhotosRepository, OwnershipGuard guard) {
        this.profilePhotosRepository = profilePhotosRepository;
        this.guard = guard;
    }

    public String AddProfilePhoto(ProfilePhotosDto profilePhotosDto){
        try {
            if(profilePhotosDto == null){
                return "Photos cannot be null";
            }
            // Nunca se reutiliza un id del body (evita sobrescribir la fila de otro usuario).
            ProfilePhotos profilePhotos = profilePhotosRepository.findByUserId(profilePhotosDto.getUserId())
                    .orElse(new ProfilePhotos(null, profilePhotosDto.getUserId(), null, null));
            profilePhotos.setPhotoUrl(profilePhotosDto.getPhotoUrl());
            profilePhotos.setWallPhotoUrl(profilePhotosDto.getWallPhotoUrl());
            profilePhotosRepository.save(profilePhotos);
            return "Photos Add successufly";
        } catch (Exception e) {
            return "Error: "+e.getMessage();
        }
    }

    public ProfilePhotos GetPhotoById(Long Id){
        try {
            if(Long.valueOf(Id) == null || Id == 0){
                return null;
            }
            Optional<ProfilePhotos> profile =  profilePhotosRepository.findById(Id);
            return profile.orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    /** Solo el propietario de las fotos (o ADMIN) las cambia (403); false si no existen. */
    public boolean UpdatePhotoUrlById(Long id, String newPhotoUrl) {
        ProfilePhotos profile = (id == null || id <= 0) ? null : profilePhotosRepository.findById(id).orElse(null);
        if (profile == null) {
            return false;
        }
        guard.assertOwnerOrAdmin(profile.getUserId());
        profile.setPhotoUrl(newPhotoUrl);
        profilePhotosRepository.save(profile);
        return true;
    }

    /** Solo el propietario de las fotos (o ADMIN) las cambia (403); false si no existen. */
    public boolean UpdateWallPhotoUrlById(Long id, String newWallPhotoUrl) {
        ProfilePhotos profile = (id == null || id <= 0) ? null : profilePhotosRepository.findById(id).orElse(null);
        if (profile == null) {
            return false;
        }
        guard.assertOwnerOrAdmin(profile.getUserId());
        profile.setWallPhotoUrl(newWallPhotoUrl);
        profilePhotosRepository.save(profile);
        return true;
    }
}
