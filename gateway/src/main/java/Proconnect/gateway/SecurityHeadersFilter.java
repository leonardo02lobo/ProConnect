package Proconnect.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;

/**
 * Headers de seguridad en TODA respuesta del gateway (rutas proxificadas, 401 del filtro JWT, 404 y actuator).
 * CSP en enforce por defecto ({@code CSP_REPORT_ONLY=true} la pasa a Report-Only); HSTS solo con
 * {@code proconnect.security.hsts.enabled=true} (perfil {@code prod}).
 */
@Component
public class SecurityHeadersFilter implements WebFilter, Ordered {

    public static final String CSP = "default-src 'self'; script-src 'self'; object-src 'none'; "
            + "img-src 'self' data:; style-src 'self'; connect-src 'self'; frame-ancestors 'none'; "
            + "base-uri 'none'; form-action 'self'";

    private final boolean reportOnly;
    private final boolean hsts;

    public SecurityHeadersFilter(@Value("${proconnect.security.csp-report-only:false}") boolean reportOnly,
            @Value("${proconnect.security.hsts.enabled:false}") boolean hsts) {
        this.reportOnly = reportOnly;
        this.hsts = hsts;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        exchange.getResponse().beforeCommit(() -> {
            HttpHeaders h = exchange.getResponse().getHeaders();
            h.remove(reportOnly ? "Content-Security-Policy" : "Content-Security-Policy-Report-Only");
            h.set(reportOnly ? "Content-Security-Policy-Report-Only" : "Content-Security-Policy", CSP);
            h.set("X-Content-Type-Options", "nosniff");
            h.set("X-Frame-Options", "DENY");
            h.set("Referrer-Policy", "strict-origin-when-cross-origin");
            h.set("Permissions-Policy", "camera=(), microphone=(), geolocation=(), payment=(), usb=(), "
                    + "interest-cohort=()");
            h.set("Cross-Origin-Opener-Policy", "same-origin");
            h.set("Cross-Origin-Resource-Policy", "same-site");
            h.remove("X-Powered-By");
            h.remove("Server");
            if (hsts) {
                h.set("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
            }
            return Mono.empty();
        });
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
