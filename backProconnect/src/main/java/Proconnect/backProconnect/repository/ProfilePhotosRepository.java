package Proconnect.backProconnect.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import Proconnect.backProconnect.models.ProfilePhotos;

@Repository
public interface ProfilePhotosRepository extends JpaRepository<ProfilePhotos, Long>{
    
}