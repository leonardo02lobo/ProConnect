package Proconnect.backProconnect.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

/** Perfil público de un usuario con sus posts. El email solo aparece para el propio usuario o un ADMIN. */
public record UserProfileDto(
        Long id,
        @JsonInclude(JsonInclude.Include.NON_NULL) String email,
        String name,
        String role,
        String userName,
        String photoUrl,
        String wallPhotoUrl,
        List<FeedPostDto> posts) {
}
