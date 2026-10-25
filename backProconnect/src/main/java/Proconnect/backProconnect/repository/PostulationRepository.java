package Proconnect.backProconnect.repository;

import Proconnect.backProconnect.Entity.Postulation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostulationRepository extends JpaRepository<Postulation, Long> {

    Slice<Postulation> findAllBy(Pageable pageable);

    Slice<Postulation> findByJobId(Long jobId, Pageable pageable);

    /** Las propias más las recibidas en empleos de empresas del usuario, en UNA query paginada. */
    @Query(
            "SELECT p FROM Postulation p WHERE p.userId = :userId OR p.jobId IN (SELECT j.id FROM"
                    + " Job j JOIN j.company c WHERE c.userId = :userId)")
    Slice<Postulation> visibleTo(@Param("userId") Long userId, Pageable pageable);
}
