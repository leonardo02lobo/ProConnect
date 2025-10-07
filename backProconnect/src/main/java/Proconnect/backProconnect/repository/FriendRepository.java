package Proconnect.backProconnect.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import Proconnect.backProconnect.Entity.Friend;

@Repository
public interface FriendRepository extends JpaRepository<Friend,Long>{
    
}