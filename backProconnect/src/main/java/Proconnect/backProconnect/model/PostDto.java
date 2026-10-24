package Proconnect.backProconnect.model;

import java.util.Date;

/** Post sin agregados (respuesta de GET/POST/PUT /api/posts/{id}). */
public record PostDto(Long id, Long userId, String title, String content, String imageUrl, Date date) {
}
