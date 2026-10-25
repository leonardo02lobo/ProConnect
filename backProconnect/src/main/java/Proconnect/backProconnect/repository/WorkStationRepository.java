package Proconnect.backProconnect.repository;

import Proconnect.backProconnect.Entity.WorkStation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkStationRepository extends JpaRepository<WorkStation, Long> {

    Slice<WorkStation> findAllBy(Pageable pageable);

    @Query("SELECT w FROM WorkStation w WHERE LOWER(w.workStation) LIKE :pattern")
    Slice<WorkStation> search(@Param("pattern") String pattern, Pageable pageable);
}
