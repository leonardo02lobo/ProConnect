package Proconnect.backProconnect.model;

import java.util.Date;

/** Comentario con los datos públicos de su autor. */
public record CommentDto(
        Long id, Long postId, Long userId, String content, Date date, UserPublicDto user) {}
