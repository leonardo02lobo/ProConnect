package Proconnect.backProconnect.model;

import java.time.Instant;

public record MessageDto(
        Long id, Long senderId, Long receiverId, String content, Instant timestamp) {}
