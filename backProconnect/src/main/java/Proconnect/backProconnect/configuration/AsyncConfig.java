package Proconnect.backProconnect.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * {@code @Async} (envío de correo fuera del request), {@code @Retryable} (reintentos SMTP) y
 * {@code @Scheduled} (reconciliación de contadores, desactivada por defecto).
 */
@Configuration
@EnableAsync
@EnableScheduling
@EnableRetry
public class AsyncConfig {}
