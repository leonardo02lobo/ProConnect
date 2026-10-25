package Proconnect.backProconnect.repository;

import Proconnect.backProconnect.Entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    /** Mensajes en los que participa el usuario (enviados o recibidos), paginados. */
    Slice<Message> findBySenderIdOrReceiverId(Long senderId, Long receiverId, Pageable pageable);
}
