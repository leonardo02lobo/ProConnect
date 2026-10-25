package Proconnect.backProconnect.exception;

/** Límite de intentos superado (429 + Retry-After). */
public class TooManyRequestsException extends RuntimeException {
    private final long retryAfterSeconds;

    public TooManyRequestsException(long retryAfterSeconds) {
        super("Demasiados intentos. Inténtalo más tarde.");
        this.retryAfterSeconds = Math.max(1, retryAfterSeconds);
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
