package Proconnect.backProconnect.repository.projection;

import Proconnect.backProconnect.model.Role;
import java.time.Instant;

/** Comentario con su autor (JPQL con {@code JOIN c.author}). */
public record CommentView(
        Long id,
        Long postId,
        Long userId,
        String content,
        Instant createdAt,
        String authorName,
        String authorUserName,
        String authorEmail,
        Role authorRole,
        String authorAvatarUrl,
        String authorCoverUrl) {}
