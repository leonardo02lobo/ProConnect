package Proconnect.backProconnect.repository;

import Proconnect.backProconnect.Entity.Postulation;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostulationRepository extends JpaRepository<Postulation, Long> {
    List<Postulation> findByUserId(Long userId);

    List<Postulation> findByJobId(Long jobId);

    List<Postulation> findByJobIdIn(Collection<Long> jobIds);

    boolean existsByUserIdAndJobId(Long userId, Long jobId);
}
