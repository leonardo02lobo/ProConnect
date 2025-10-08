package Proconnect.backProconnect.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import Proconnect.backProconnect.model.JobDto;

@Repository
public interface JobRepository extends JpaRepository<JobDto, Long>, JpaSpecificationExecutor<JobDto>{

}