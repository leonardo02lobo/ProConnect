package Proconnect.backProconnect.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import Proconnect.backProconnect.Entity.User;

@Repository
public interface UserRepository  extends JpaRepository<User, Long>, JpaSpecificationExecutor<User>{
    Optional<User> findByUserName(String userName);
    Optional<User> findByEmail(String email);
}