package Proconnect.backProconnect.repository;

import Proconnect.backProconnect.Entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {
    java.util.List<Job> findByCompanyIdIn(java.util.Collection<Long> companyIds);
}
