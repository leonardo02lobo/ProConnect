package Proconnect.backProconnect.model;

import java.time.Instant;
import java.util.List;

/** Post del feed o de un perfil: autor, contadores, si el viewer dio like y comentarios. */
public record FeedPostDto(
        Long id,
        Long userId,
        UserPublicDto user,
        String title,
        String content,
        String imageUrl,
        Instant date,
        long totalLikes,
        long totalComments,
        boolean userLiked,
        List<CommentDto> comments) {}
