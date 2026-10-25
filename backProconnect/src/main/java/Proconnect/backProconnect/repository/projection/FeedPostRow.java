package Proconnect.backProconnect.repository.projection;

import Proconnect.backProconnect.model.Role;
import java.time.Instant;

/**
 * Post con su autor y contadores, leído en UNA query (JPQL con {@code JOIN p.author}) para el feed
 * y el perfil.
 */
public record FeedPostRow(
        Long id,
        String title,
        String content,
        String imageUrl,
        Instant createdAt,
        int likeCount,
        int commentCount,
        Long authorId,
        String authorName,
        String authorUserName,
        String authorEmail,
        Role authorRole,
        String authorAvatarUrl,
        String authorCoverUrl) {}
