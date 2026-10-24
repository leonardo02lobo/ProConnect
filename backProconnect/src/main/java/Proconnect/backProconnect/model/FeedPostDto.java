package Proconnect.backProconnect.model;

import java.util.Date;
import java.util.List;

/** Post del feed o de un perfil: autor, contadores, si el viewer dio like y comentarios. */
public record FeedPostDto(
        Long id,
        Long userId,
        UserPublicDto user,
        String title,
        String content,
        String imageUrl,
        Date date,
        long totalLikes,
        long totalComments,
        boolean userLiked,
        List<CommentDto> comments) {
}
