package Proconnect.backProconnect.model;

import java.time.Instant;

public record LikeDto(Long id, Long userId, Long postId, Instant date) {}
