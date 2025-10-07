package Proconnect.backProconnect.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import Proconnect.backProconnect.model.ProfilePhotosDto;
import Proconnect.backProconnect.repository.ProfilePhotosRepository;

@Service
public class ProfilePhotosService {
    
    private final ProfilePhotosRepository profilePhotosRepository;

    public ProfilePhotosService(ProfilePhotosRepository profilePhotosRepository) {
        this.profilePhotosRepository = profilePhotosRepository;
    }

    public String AddProfilePhoto(ProfilePhotosDto profilePhotosDto){
        try {
            if(profilePhotosDto == null){
                return "Photos cannot be null";
            }
            profilePhotosRepository.save(profilePhotosDto);
            return "Photos Add successufly";
        } catch (Exception e) {
            return "Error: "+e.getMessage();
        }
    }

    public ProfilePhotosDto GetPhotoById(Long Id){
        try {
            if(Long.valueOf(Id) == null || Id == 0){
                return null;
            }
            Optional<ProfilePhotosDto> profile =  profilePhotosRepository.findById(Id);
            return profile.orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean UpdatePhotoUrlById(Long id, String newPhotoUrl) {
        try {
            if (id == null || id == 0) {
                return false;
            }
            Optional<ProfilePhotosDto> profileOpt = profilePhotosRepository.findById(id);
            if (!profileOpt.isPresent()) {
                return false;
            }
            ProfilePhotosDto profile = profileOpt.get();
            profile.setPhotoUrl(newPhotoUrl);
            profilePhotosRepository.save(profile); 
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean UpdateWallPhotoUrlById(Long id, String newWallPhotoUrl) {
        try {
            if (id == null || id == 0) {
                return false;
            }
            Optional<ProfilePhotosDto> profileOpt = profilePhotosRepository.findById(id);
            if (!profileOpt.isPresent()) {
                return false;
            }
            ProfilePhotosDto profile = profileOpt.get();
            profile.setWallPhotoUrl(newWallPhotoUrl);
            profilePhotosRepository.save(profile); 
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
