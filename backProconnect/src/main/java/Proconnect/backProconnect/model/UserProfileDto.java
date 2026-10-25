package Proconnect.backProconnect.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/**
 * Perfil público de un usuario con sus posts. El email solo aparece para el propio usuario o un
 * ADMIN.
 */
public record UserProfileDto(
        Long id,
        @JsonInclude(JsonInclude.Include.NON_NULL) String email,
        String name,
        String role,
        String userName,
        String photoUrl,
        String wallPhotoUrl,
        List<FeedPostDto> posts) {}
