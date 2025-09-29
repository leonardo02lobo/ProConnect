package Proconnect.backProconnect.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import Proconnect.backProconnect.models.FollowCompany;

@Repository
public interface FollowCompanyRepository extends JpaRepository<FollowCompany, Long> {

}