package Proconnect.backProconnect.service;

import Proconnect.backProconnect.Entity.Message;
import Proconnect.backProconnect.exception.NotFoundException;
import Proconnect.backProconnect.model.MessageDto;
import Proconnect.backProconnect.model.MessageRequest;
import Proconnect.backProconnect.model.PageResponse;
import Proconnect.backProconnect.repository.MessageRepository;
import Proconnect.backProconnect.repository.UserRepository;
import Proconnect.backProconnect.security.OwnershipGuard;
import java.time.Instant;
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
                                Instant.now()));
        return toDto(saved);
    }

    /** Mensajes en los que participa el usuario (el controller pasa el id de la sesión). */
    @Transactional(readOnly = true)
    public PageResponse<MessageDto> listFor(Long userId, Integer page, Integer size) {
        return PageResponse.of(
                messageRepository.findBySenderIdOrReceiverId(
                        userId, userId, Paging.page(page, size)),
                MessageService::toDto);
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
                m.getId(), m.getSenderId(), m.getReceiverId(), m.getContent(), m.getSentAt());
    }
}
