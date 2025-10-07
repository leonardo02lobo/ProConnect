package Proconnect.backProconnect.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import Proconnect.backProconnect.model.ProfilePhotosDto;

@Repository
public interface ProfilePhotosRepository extends JpaRepository<ProfilePhotosDto, Long>{
    
}