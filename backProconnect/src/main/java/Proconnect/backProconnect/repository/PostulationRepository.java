package Proconnect.backProconnect.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import Proconnect.backProconnect.model.PostulationDto;

@Repository
public interface PostulationRepository extends JpaRepository<PostulationDto, Long>{

}