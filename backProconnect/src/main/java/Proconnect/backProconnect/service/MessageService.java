package Proconnect.backProconnect.service;

import java.util.List;

import org.springframework.stereotype.Service;

import Proconnect.backProconnect.Entity.Message;
import Proconnect.backProconnect.model.MessageDto;
import Proconnect.backProconnect.repository.MessageRepository;
import Proconnect.backProconnect.exception.ResourceNotFoundException;
import Proconnect.backProconnect.security.OwnershipGuard;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final OwnershipGuard guard;

    public MessageService(MessageRepository messageRepository, OwnershipGuard guard) {
        this.messageRepository = messageRepository;
        this.guard = guard;
    }

    public String AddMessage(MessageDto message) {
        try {
            if (message == null) {
                return "Message cannot be null";
            }
            Message messageEntity = new Message(null, message.getSenderId(), message.getReceiverId(), message.getContent(),
                    message.getTimestamp() != null ? message.getTimestamp() : new java.util.Date());
            messageRepository.save(messageEntity);
            return "Message Add successufly";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    /** Solo los mensajes en los que participa el usuario indicado (el controller pasa el id del token). */
    public List<MessageDto> ListMessagesByUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID must be a positive number.");
        }
        return messageRepository.findBySenderIdOrReceiverIdOrderByIdAsc(userId, userId).stream()
                .map(m -> new MessageDto(m.getId(), m.getSenderId(), m.getReceiverId(), m.getContent(),
                        m.getTimestamp()))
                .toList();
    }

    /** Solo el emisor (o ADMIN) borra; el resto recibe 404, igual que un id inexistente. */
    public boolean DeleteMessageById(Long Id) {
        Message message = (Id == null || Id <= 0) ? null : messageRepository.findById(Id).orElse(null);
        if (message == null) {
            throw new ResourceNotFoundException();
        }
        guard.assertParticipantOrNotFound(message.getSenderId());
        messageRepository.delete(message);
        return true;
    }
}
