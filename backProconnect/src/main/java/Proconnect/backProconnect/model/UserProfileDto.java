package Proconnect.backProconnect.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/**
 * Perfil público de un usuario con la primera página de sus posts ({@code nextCursor} para las
 * siguientes: {@code GET /api/users/{id}/posts?cursor=}). El email solo aparece para el propio
 * usuario o un ADMIN.
 */
public record UserProfileDto(
        Long id,
        @JsonInclude(JsonInclude.Include.NON_NULL) String email,
        String name,
        String role,
        String userName,
        String avatarUrl,
        String coverUrl,
        List<FeedPostDto> posts,
        String nextCursor) {}
