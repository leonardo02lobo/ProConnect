package Proconnect.backProconnect.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;

/** {@code @Async} (envío de correo fuera del request) y {@code @Retryable} (reintentos SMTP). */
@Configuration
@EnableAsync
@EnableRetry
public class AsyncConfig {}
