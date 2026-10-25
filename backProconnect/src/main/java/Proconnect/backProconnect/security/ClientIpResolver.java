package Proconnect.backProconnect.security;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.util.matcher.IpAddressMatcher;
import org.springframework.stereotype.Component;

/**
 * IP real del cliente. {@code X-Forwarded-For} solo se tiene en cuenta si la conexión viene de un
 * proxy de confianza (el gateway), configurado en {@code TRUSTED_PROXIES} (IPs o CIDR separados por
 * comas). Se recorre la cabecera de derecha a izquierda y se devuelve la primera IP que no es de un
 * proxy de confianza, así un cliente no puede falsificar su IP anteponiendo valores.
 */
@Component
public class ClientIpResolver {

    private final List<IpAddressMatcher> trusted;

    public ClientIpResolver(
            @Value("${proconnect.security.trusted-proxies:127.0.0.1/32,::1/128}") String proxies) {
        this.trusted =
                Arrays.stream(proxies.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(IpAddressMatcher::new)
                        .toList();
    }

    public String resolve(HttpServletRequest request) {
        String remote = request.getRemoteAddr();
        if (!isTrusted(remote)) {
            return remote;
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded == null || forwarded.isBlank()) {
            return remote;
        }
        String[] hops = forwarded.split(",");
        for (int i = hops.length - 1; i >= 0; i--) {
            String hop = hops[i].trim();
            if (!hop.isEmpty() && !isTrusted(hop)) {
                return hop;
            }
        }
        return remote;
    }

    private boolean isTrusted(String ip) {
        if (ip == null) {
            return false;
        }
        for (IpAddressMatcher matcher : trusted) {
            try {
                if (matcher.matches(ip)) {
                    return true;
                }
            } catch (IllegalArgumentException notAnIp) {
                return false;
            }
        }
        return false;
    }
}
