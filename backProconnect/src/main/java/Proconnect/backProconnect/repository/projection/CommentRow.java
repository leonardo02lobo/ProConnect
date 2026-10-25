package Proconnect.backProconnect.repository.projection;

import java.time.Instant;

/** Comentario con su autor leído por alias desde la query nativa del preview del feed. */
public interface CommentRow {
    Long getId();

    Long getPostId();

    Long getUserId();

    String getContent();

    Instant getCreatedAt();

    String getAuthorName();

    String getAuthorUserName();

    String getAuthorEmail();

    String getAuthorRole();

    String getAuthorAvatarUrl();

    String getAuthorCoverUrl();
}
