package Proconnect.gateway;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Exige un JWT válido (firma HS256 + expiración) en la cookie de sesión para todo /upload/**. No
 * consulta la base de datos: la autorización fina sigue en la API core.
 */
@Component
public class MediaJwtFilter implements GlobalFilter, Ordered {

    private static final String PROTECTED_PREFIX = "/upload/";

    private final SecretKey key;
    private final String cookieName;

    public MediaJwtFilter(
            @Value("${proconnect.jwt.secret:}") String secret,
            @Value("${proconnect.jwt.cookie-name:pc_session}") String cookieName) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "JWT_SECRET no está definido: el gateway no puede validar sesiones en /upload/**");
        }
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException(
                    "JWT_SECRET debe tener al menos 32 bytes (tiene " + bytes.length + ")");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
        this.cookieName = cookieName;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getRawPath();
        if (!path.startsWith(PROTECTED_PREFIX)
                || HttpMethod.OPTIONS.equals(exchange.getRequest().getMethod())) {
            return chain.filter(exchange);
        }
        HttpCookie cookie = exchange.getRequest().getCookies().getFirst(cookieName);
        if (cookie == null || cookie.getValue().isBlank() || !isValid(cookie.getValue())) {
            return unauthorized(exchange.getResponse());
        }
        return chain.filter(exchange);
    }

    private boolean isValid(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Mono<Void> unauthorized(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] body =
                "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Sesión requerida\"}"
                        .getBytes(StandardCharsets.UTF_8);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body)));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
