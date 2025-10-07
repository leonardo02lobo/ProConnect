package Proconnect.backProconnect.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import Proconnect.backProconnect.model.WorkStationDto;

@Repository
public interface WorkStationRepository extends JpaRepository<WorkStationDto, Long>,JpaSpecificationExecutor<WorkStationDto>{
    
}