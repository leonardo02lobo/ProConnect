package Proconnect.backProconnect.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/** Límites de paginación comunes (spec feed-performance). */
public final class Paging {

    /** Feed, posts de perfil y comentarios: por defecto 20, máximo 50. */
    public static final int DEFAULT_CURSOR_LIMIT = 20;

    public static final int MAX_CURSOR_LIMIT = 50;

    /** Listados {@code ?page=&size=}: por defecto 20, máximo 100. */
    public static final int DEFAULT_PAGE_SIZE = 20;

    public static final int MAX_PAGE_SIZE = 100;

    /** Comentarios por post en el feed/perfil. */
    public static final int COMMENTS_PREVIEW = 3;

    private Paging() {}

    public static int cursorLimit(Integer requested) {
        if (requested == null || requested < 1) {
            return DEFAULT_CURSOR_LIMIT;
        }
        return Math.min(requested, MAX_CURSOR_LIMIT);
    }

    /** Página 0-based con tamaño acotado y orden estable por id ascendente. */
    public static Pageable page(Integer page, Integer size) {
        return page(page, size, Sort.by("id"));
    }

    public static Pageable page(Integer page, Integer size, Sort sort) {
        int p = page == null || page < 0 ? 0 : page;
        int s = size == null || size < 1 ? DEFAULT_PAGE_SIZE : Math.min(size, MAX_PAGE_SIZE);
        return PageRequest.of(p, s, sort);
    }
}
