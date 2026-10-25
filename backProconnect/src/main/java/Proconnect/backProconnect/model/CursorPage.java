package Proconnect.backProconnect.model;

import java.util.List;

/**
 * Página por cursor (feed, posts de un perfil, comentarios de un post). {@code nextCursor} es opaco
 * para el cliente y es {@code null} en la última página.
 */
public record CursorPage<T>(List<T> items, String nextCursor) {}
