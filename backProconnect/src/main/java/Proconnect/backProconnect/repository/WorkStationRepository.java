package Proconnect.backProconnect.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import Proconnect.backProconnect.Entity.WorkStation;

@Repository
public interface WorkStationRepository extends JpaRepository<WorkStation, Long>,JpaSpecificationExecutor<WorkStation>{
    
}