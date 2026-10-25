package Proconnect.backProconnect.repository;

import Proconnect.backProconnect.Entity.Message;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    /** Mensajes en los que participa el usuario (enviados o recibidos). */
    List<Message> findBySenderIdOrReceiverIdOrderByIdAsc(Long senderId, Long receiverId);
}
