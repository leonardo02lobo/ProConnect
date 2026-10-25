package Proconnect.backProconnect.repository;

import Proconnect.backProconnect.Entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    java.util.List<Company> findByUserId(Long userId);
}
