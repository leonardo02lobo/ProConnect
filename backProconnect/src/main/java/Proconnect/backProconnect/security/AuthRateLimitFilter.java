package Proconnect.backProconnect.security;

import Proconnect.backProconnect.exception.ProblemResponses;
import Proconnect.backProconnect.exception.TooManyRequestsException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

/** Límite por IP en POST /api/auth/login y /api/auth/register: 429 + Retry-After al superarlo. */
public class AuthRateLimitFilter extends OncePerRequestFilter {

    /** Mensaje fijo (verify-web-security): nunca se refleja texto de la excepción en el cuerpo. */
    private static final String TOO_MANY_DETAIL = "Demasiados intentos. Inténtalo más tarde.";

    private final AuthRateLimiter limiter;
    private final ClientIpResolver ipResolver;

    public AuthRateLimitFilter(AuthRateLimiter limiter, ClientIpResolver ipResolver) {
        this.limiter = limiter;
        this.ipResolver = ipResolver;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !"POST".equalsIgnoreCase(request.getMethod())
                || !(path.equals("/api/auth/login") || path.equals("/api/auth/register"));
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            limiter.consumeIp(ipResolver.resolve(request));
        } catch (TooManyRequestsException e) {
            response.setHeader("Retry-After", String.valueOf(e.getRetryAfterSeconds()));
            ProblemResponses.write(
                    request, response, HttpStatus.TOO_MANY_REQUESTS, TOO_MANY_DETAIL);
            return;
        }
        chain.doFilter(request, response);
    }
}
