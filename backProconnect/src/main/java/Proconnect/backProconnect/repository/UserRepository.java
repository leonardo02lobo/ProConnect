package Proconnect.backProconnect.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import Proconnect.backProconnect.models.User;

@Repository
public interface UserRepository  extends JpaRepository<User, Long>, JpaSpecificationExecutor<User>{
    
}