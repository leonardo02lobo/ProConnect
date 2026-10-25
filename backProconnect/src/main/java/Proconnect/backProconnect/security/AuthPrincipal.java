package Proconnect.backProconnect.security;

import Proconnect.backProconnect.model.Role;

/** Usuario autenticado, extraído del JWT de la cookie de sesión. */
public record AuthPrincipal(Long userId, Role role, String name) {}
