package Proconnect.backProconnect.repository;

import Proconnect.backProconnect.Entity.Job;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    Slice<Job> findAllBy(Pageable pageable);

    /** Empleos que contienen el texto en el título o la descripción (sin distinguir mayúsculas). */
    @Query(
            "SELECT j FROM Job j WHERE LOWER(j.title) LIKE :pattern OR LOWER(j.description) LIKE"
                    + " :pattern")
    Slice<Job> search(@Param("pattern") String pattern, Pageable pageable);
}
