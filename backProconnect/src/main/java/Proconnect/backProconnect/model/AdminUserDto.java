package Proconnect.backProconnect.model;

/** Usuario en las rutas de administración (/api/admin/users): incluye email y rol. */
public record AdminUserDto(Long id, String name, String userName, String email, String role) {}
