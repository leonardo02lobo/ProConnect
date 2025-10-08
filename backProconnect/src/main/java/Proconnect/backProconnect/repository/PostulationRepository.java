package Proconnect.backProconnect.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import Proconnect.backProconnect.Entity.Postulation;

@Repository
public interface PostulationRepository extends JpaRepository<Postulation, Long>{

}