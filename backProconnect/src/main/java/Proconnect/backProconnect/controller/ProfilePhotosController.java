package Proconnect.backProconnect.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import Proconnect.backProconnect.Entity.ProfilePhotos;
import Proconnect.backProconnect.model.ProfilePhotosDto;
import Proconnect.backProconnect.security.CurrentUser;
import Proconnect.backProconnect.service.ProfilePhotosService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/profilePhoto")
public class ProfilePhotosController {
    
    private final ProfilePhotosService profilePhotosService;

    public ProfilePhotosController(ProfilePhotosService profilePhotosService){
        this.profilePhotosService = profilePhotosService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> AddProfilePhoto(@RequestBody @Valid ProfilePhotosDto profilePhotosDto){
        profilePhotosDto.setUserId(CurrentUser.id());
        String result = profilePhotosService.AddProfilePhoto(profilePhotosDto);
        if (result == null) {
            return ResponseEntity.status(400).body("Failed to create profile photo");
        }
        return ResponseEntity.status(201).body(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> GetPhotoById(@PathVariable Long Id){
        ProfilePhotos photo = profilePhotosService.GetPhotoById(Id);
        if (photo == null) {
            return ResponseEntity.status(404).body("Photo not found");
        }
        return ResponseEntity.ok().body(photo);
    }

    @PostMapping("/updateProfilePhoto")
    public ResponseEntity<?> UpdatePhotoUrlById(@RequestBody @Valid ProfilePhotosDto profilePhotosDto){
        boolean result = profilePhotosService.UpdatePhotoUrlById(profilePhotosDto.getId(), profilePhotosDto.getPhotoUrl());
        if(result){
            return ResponseEntity.ok().body("Profile Photo Update successufly");
        }else{
            return ResponseEntity.badRequest().body("Profile Photo does not successufly");
        }
    }

    @PostMapping("/updateWallPhoto")
    public ResponseEntity<?> UpdateWallPhotoUrlById(@RequestBody @Valid ProfilePhotosDto profilePhotosDto){
        boolean result = profilePhotosService.UpdateWallPhotoUrlById(profilePhotosDto.getId(), profilePhotosDto.getWallPhotoUrl());
        if(result){
            return ResponseEntity.ok().body("Profile Photo Update successufly");
        }else{
            return ResponseEntity.badRequest().body("Profile Photo does not successufly");
        }
    }
}
