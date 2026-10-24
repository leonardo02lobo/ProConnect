package Proconnect.backProconnect.model;

import java.util.Date;

public record MessageDto(Long id, Long senderId, Long receiverId, String content, Date timestamp) {
}
