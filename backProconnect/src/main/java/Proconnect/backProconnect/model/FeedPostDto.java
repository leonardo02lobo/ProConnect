package Proconnect.backProconnect.model;

import java.time.Instant;
import java.util.List;

/**
 * Post del feed o de un perfil: autor, contadores denormalizados ({@code likeCount}, {@code
 * commentCount}), si el viewer dio like y los últimos {@value
 * Proconnect.backProconnect.service.Paging#COMMENTS_PREVIEW} comentarios ({@code commentsPreview},
 * en orden cronológico). La lista completa: {@code GET /api/posts/{id}/comments}.
 */
public record FeedPostDto(
        Long id,
        Long userId,
        UserPublicDto user,
        String title,
        String content,
        String imageUrl,
        Instant date,
        long likeCount,
        long commentCount,
        boolean userLiked,
        List<CommentDto> commentsPreview) {}
