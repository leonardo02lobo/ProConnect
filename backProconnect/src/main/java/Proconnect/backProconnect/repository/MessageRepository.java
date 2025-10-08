package Proconnect.backProconnect.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import Proconnect.backProconnect.model.MessageDto;

@Repository
public interface MessageRepository extends JpaRepository<MessageDto, Long> {
    List<MessageDto> findByUserId(Long userId);
}