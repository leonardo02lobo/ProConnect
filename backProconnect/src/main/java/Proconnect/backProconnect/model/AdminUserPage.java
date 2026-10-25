package Proconnect.backProconnect.model;

import java.util.List;

/** Página de {@code GET /api/admin/users}. {@code page} empieza en 0. */
public record AdminUserPage(
        List<AdminUserDto> content, int page, int size, long totalElements, int totalPages) {}
