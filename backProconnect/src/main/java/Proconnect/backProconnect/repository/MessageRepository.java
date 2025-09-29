package Proconnect.backProconnect.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import Proconnect.backProconnect.models.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
}