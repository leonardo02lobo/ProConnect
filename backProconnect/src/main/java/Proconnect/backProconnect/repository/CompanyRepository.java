package Proconnect.backProconnect.repository;

import Proconnect.backProconnect.Entity.Company;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    Slice<Company> findAllBy(Pageable pageable);
}
