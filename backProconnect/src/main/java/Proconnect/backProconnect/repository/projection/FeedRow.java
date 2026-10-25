package Proconnect.backProconnect.repository.projection;

import java.util.Date;

/**
 * Fila del feed (post + autor + contadores) leída por alias desde la query nativa de {@code
 * PostsRepository}.
 */
public interface FeedRow {
    Long getId();

    String getTitle();

    String getContent();

    Date getDate();

    String getImageUrl();

    Long getUserId();

    String getUserEmail();

    String getUserName();

    String getUserRole();

    String getUserUserName();

    String getPhotoUrl();

    String getWallPhotoUrl();

    Long getTotalLikes();

    Long getTotalComments();

    Long getUserLiked();
}
