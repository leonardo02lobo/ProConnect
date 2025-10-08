package Proconnect.backProconnect.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import Proconnect.backProconnect.Entity.Message;
import Proconnect.backProconnect.model.MessageDto;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<MessageDto> findBysenderId(Long senderId);
}