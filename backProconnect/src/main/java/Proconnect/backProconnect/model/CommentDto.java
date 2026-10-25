package Proconnect.backProconnect.model;

import java.time.Instant;

/** Comentario con los datos públicos de su autor. */
public record CommentDto(
        Long id, Long postId, Long userId, String content, Instant date, UserPublicDto user) {}
