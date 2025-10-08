package Proconnect.backProconnect.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import Proconnect.backProconnect.model.CompanyDto;

@Repository
public interface CompanyRepository extends JpaRepository<CompanyDto,Long>{
    
}
