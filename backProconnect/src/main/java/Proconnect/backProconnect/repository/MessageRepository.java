package Proconnect.backProconnect.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import Proconnect.backProconnect.Entity.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
}