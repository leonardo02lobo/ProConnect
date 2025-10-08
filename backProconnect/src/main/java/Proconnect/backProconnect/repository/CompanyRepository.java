package Proconnect.backProconnect.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import Proconnect.backProconnect.Entity.Company;

@Repository
public interface CompanyRepository extends JpaRepository<Company,Long>{
    
}
