package Proconnect.backProconnect.security;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import Proconnect.backProconnect.exception.TooManyRequestsException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Límite por IP en POST /api/auth/login y /api/auth/register: 429 + Retry-After al superarlo. */
public class AuthRateLimitFilter extends OncePerRequestFilter {

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
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            limiter.consumeIp(ipResolver.resolve(request));
        } catch (TooManyRequestsException e) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("Retry-After", String.valueOf(e.getRetryAfterSeconds()));
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"status\":429,\"error\":\"Too Many Requests\",\"message\":\""
                    + e.getMessage() + "\"}");
            return;
        }
        chain.doFilter(request, response);
    }
}
