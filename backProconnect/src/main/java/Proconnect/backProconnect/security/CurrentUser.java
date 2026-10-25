package Proconnect.backProconnect.security;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** Acceso al actor autenticado. La autoría de toda mutación sale de aquí, nunca del body. */
public final class CurrentUser {

    private CurrentUser() {}

    public static AuthPrincipal principal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof AuthPrincipal principal)) {
            throw new AuthenticationCredentialsNotFoundException("No hay sesión");
        }
        return principal;
    }

    public static Long id() {
        return principal().userId();
    }
}
