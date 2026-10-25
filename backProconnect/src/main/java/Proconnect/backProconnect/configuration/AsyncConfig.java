package Proconnect.backProconnect.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * {@code @Async} (envío de correo fuera del request) y {@code @Scheduled} (reconciliación de
 * contadores, desactivada por defecto). Los reintentos SMTP usan el {@code RetryTemplate} del core
 * de Spring Framework 7 (ver {@code ApplicationMailService}).
 */
@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig {}
