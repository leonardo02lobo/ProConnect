package Proconnect.backProconnect.repository.projection;

import java.time.Instant;

/** Comentario con los datos públicos de su autor (query nativa de {@code CommentsRepository}). */
public interface CommentRow {
    Long getId();

    Long getPostId();

    Long getUserId();

    String getContent();

    Instant getDate();

    String getUserEmail();

    String getUserName();

    String getUserRole();

    String getUserUserName();

    String getPhotoUrl();

    String getWallPhotoUrl();
}
