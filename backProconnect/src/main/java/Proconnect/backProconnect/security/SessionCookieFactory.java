package Proconnect.backProconnect.security;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/** Construye la cookie pc_session: HttpOnly; Path=/; SameSite=Lax; Secure salvo COOKIE_SECURE=false. */
@Component
public class SessionCookieFactory {

    private final String name;
    private final boolean secure;

    public SessionCookieFactory(@Value("${proconnect.cookie.name:pc_session}") String name,
            @Value("${proconnect.cookie.secure:true}") boolean secure) {
        this.name = name;
        this.secure = secure;
    }

    public ResponseCookie session(String token, Duration maxAge) {
        return base(token).maxAge(maxAge).build();
    }

    public ResponseCookie expired() {
        return base("").maxAge(0).build();
    }

    private ResponseCookie.ResponseCookieBuilder base(String value) {
        return ResponseCookie.from(name, value).httpOnly(true).secure(secure).path("/").sameSite("Lax");
    }
}
