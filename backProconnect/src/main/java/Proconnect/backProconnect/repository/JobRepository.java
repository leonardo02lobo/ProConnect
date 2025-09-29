package Proconnect.backProconnect.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import Proconnect.backProconnect.models.Job;

@Repository
public interface JobRepository extends JpaRepository<Job, Long>{

}