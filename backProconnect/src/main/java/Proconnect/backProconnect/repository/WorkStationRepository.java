package Proconnect.backProconnect.repository;

import Proconnect.backProconnect.Entity.WorkStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkStationRepository
        extends JpaRepository<WorkStation, Long>, JpaSpecificationExecutor<WorkStation> {}
