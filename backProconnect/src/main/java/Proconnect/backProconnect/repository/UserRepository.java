package Proconnect.backProconnect.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import Proconnect.backProconnect.model.UserDto;

@Repository
public interface UserRepository  extends JpaRepository<UserDto, Long>, JpaSpecificationExecutor<UserDto>{
    UserDto findByUserName(String userName);
}