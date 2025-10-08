package Proconnect.backProconnect.service;

import java.util.List;

import org.springframework.stereotype.Service;

import Proconnect.backProconnect.Entity.Message;
import Proconnect.backProconnect.model.MessageDto;
import Proconnect.backProconnect.repository.MessageRepository;

@Service
public class MessageService {

    private final MessageRepository messageRepository;

    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public String AddMessage(MessageDto message) {
        try {
            if (message == null) {
                return "Message cannot be null";
            }
            Message messageEntity = new Message(message.getId(), message.getSenderId(), message.getReceiverId(), message.getContent(), message.getTimestamp());
            messageRepository.save(messageEntity);
            return "Message Add successufly";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    public List<MessageDto> ListMessagesByUserId(Long senderId) {
        try {
            if (senderId == null || senderId <= 0) {
                throw new IllegalArgumentException("User ID must be a positive number.");
            }

            return messageRepository.findBysenderId(senderId);
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving messages for user ID: " + senderId, e);
        }
    }

    public boolean DeleteMessageById(Long Id) {
        try {
            if(Id == null || Id <= 0){
                return false;
            }
            messageRepository.deleteById(Id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
