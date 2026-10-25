package Proconnect.backProconnect.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Lee la cookie de sesión, valida el JWT y llena el SecurityContext con userId y rol. El rol es el
 * vigente en la BD ({@link RoleResolver}), no el claim del token: un cambio de rol se aplica a las
 * sesiones abiertas sin re-login. Si el usuario del token ya no existe, la petición queda sin
 * autenticar (401 en rutas protegidas).
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final RoleResolver roleResolver;
    private final String cookieName;

    public JwtAuthFilter(
            JwtService jwtService,
            RoleResolver roleResolver,
            @Value("${proconnect.cookie.name:pc_session}") String cookieName) {
        this.jwtService = jwtService;
        this.roleResolver = roleResolver;
        this.cookieName = cookieName;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String token = readCookie(request);
        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            jwtService
                    .parse(token)
                    .flatMap(this::withCurrentRole)
                    .ifPresent(JwtAuthFilter::authenticate);
        }
        chain.doFilter(request, response);
    }

    /** Sustituye el rol del claim por el vigente en la BD; vacío si el usuario ya no existe. */
    private Optional<AuthPrincipal> withCurrentRole(AuthPrincipal fromToken) {
        return roleResolver
                .currentRole(fromToken.userId())
                .map(role -> new AuthPrincipal(fromToken.userId(), role, fromToken.name()));
    }

    private static void authenticate(AuthPrincipal principal) {
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + principal.role().name()));
        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(principal, null, authorities));
    }

    private String readCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName()) && !cookie.getValue().isBlank()) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
