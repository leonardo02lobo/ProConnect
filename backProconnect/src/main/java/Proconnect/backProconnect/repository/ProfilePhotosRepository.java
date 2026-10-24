package Proconnect.backProconnect.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import Proconnect.backProconnect.Entity.ProfilePhotos;

@Repository
public interface ProfilePhotosRepository extends JpaRepository<ProfilePhotos, Long> {
    /** La fila más reciente: datos legacy pueden tener varias por usuario. */
    Optional<ProfilePhotos> findFirstByUserIdOrderByIdDesc(Long userId);
}
