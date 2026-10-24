package Proconnect.backProconnect.security;

import Proconnect.backProconnect.exception.ProblemResponses;

import java.io.IOException;
import java.net.URI;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Defensa CSRF sin estado: rechaza con 403 las mutaciones (POST/PUT/PATCH/DELETE) cuyo {@code Origin} (o, en su
 * ausencia, el origen del {@code Referer}) no esté en {@code CORS_ORIGINS}. Un {@code Origin: null} se rechaza.
 * Si no llega ninguna de las dos cabeceras (clientes no navegador, SSR) se deja pasar: los navegadores
 * siempre envían {@code Origin} en mutaciones y la cookie {@code SameSite=Lax} no viaja en peticiones cross-site.
 */
public class OriginCheckFilter extends OncePerRequestFilter {

    private static final Set<String> MUTATIONS = Set.of("POST", "PUT", "PATCH", "DELETE");

    private final Set<String> allowedOrigins;

    public OriginCheckFilter(Set<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !MUTATIONS.contains(request.getMethod().toUpperCase());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String origin = request.getHeader("Origin");
        if (origin == null) {
            origin = originOf(request.getHeader("Referer"));
        }
        if (origin != null && !allowedOrigins.contains(normalize(origin))) {
            ProblemResponses.write(request, response, HttpStatus.FORBIDDEN, "Origen no permitido");
            return;
        }
        chain.doFilter(request, response);
    }

    static String normalize(String origin) {
        String o = origin.trim();
        while (o.endsWith("/")) {
            o = o.substring(0, o.length() - 1);
        }
        return o.toLowerCase();
    }

    /** Origen (scheme://host[:port]) de una URL de Referer; "null" si no se puede interpretar. */
    static String originOf(String referer) {
        if (referer == null || referer.isBlank()) {
            return null;
        }
        try {
            URI uri = URI.create(referer.trim());
            if (uri.getScheme() == null || uri.getHost() == null) {
                return "null";
            }
            return uri.getScheme() + "://" + uri.getHost() + (uri.getPort() == -1 ? "" : ":" + uri.getPort());
        } catch (IllegalArgumentException e) {
            return "null";
        }
    }
}
