package Proconnect.backProconnect.service;

import java.util.List;

import org.springframework.stereotype.Service;

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
            messageRepository.save(message);
            return "Message Add successufly";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    public List<MessageDto> ListMessagesByUserId(Long userId) {
        try {
            if (userId == null || userId <= 0) {
                throw new IllegalArgumentException("User ID must be a positive number.");
            }

            return messageRepository.findByUserId(userId);
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving messages for user ID: " + userId, e);
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
