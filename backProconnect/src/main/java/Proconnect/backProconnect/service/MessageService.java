package Proconnect.backProconnect.service;

import Proconnect.backProconnect.Entity.Message;
import Proconnect.backProconnect.exception.NotFoundException;
import Proconnect.backProconnect.model.MessageDto;
import Proconnect.backProconnect.model.MessageRequest;
import Proconnect.backProconnect.repository.MessageRepository;
import Proconnect.backProconnect.repository.UserRepository;
import Proconnect.backProconnect.security.OwnershipGuard;
import java.util.Date;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final OwnershipGuard guard;

    public MessageService(
            MessageRepository messageRepository,
            UserRepository userRepository,
            OwnershipGuard guard) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.guard = guard;
    }

    /** El emisor sale de la sesión; 404 si el destinatario no existe. */
    @Transactional
    public MessageDto send(Long senderId, MessageRequest request) {
        if (!userRepository.existsById(request.receiverId())) {
            throw new NotFoundException("Usuario no encontrado");
        }
        Message saved =
                messageRepository.save(
                        new Message(
                                null,
                                senderId,
                                request.receiverId(),
                                request.content().trim(),
                                new Date()));
        return toDto(saved);
    }

    /** Mensajes en los que participa el usuario (el controller pasa el id de la sesión). */
    @Transactional(readOnly = true)
    public List<MessageDto> listFor(Long userId) {
        return messageRepository.findBySenderIdOrReceiverIdOrderByIdAsc(userId, userId).stream()
                .map(MessageService::toDto)
                .toList();
    }

    /** Solo el emisor (o ADMIN) borra; el resto recibe 404, igual que un id inexistente. */
    @Transactional
    public void delete(Long id) {
        Message message =
                (id == null || id <= 0) ? null : messageRepository.findById(id).orElse(null);
        if (message == null) {
            throw new NotFoundException();
        }
        guard.assertParticipantOrNotFound(message.getSenderId());
        messageRepository.delete(message);
    }

    private static MessageDto toDto(Message m) {
        return new MessageDto(
                m.getId(), m.getSenderId(), m.getReceiverId(), m.getContent(), m.getTimestamp());
    }
}
