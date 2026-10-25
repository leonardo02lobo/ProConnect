package Proconnect.backProconnect.configuration;

import Proconnect.backProconnect.exception.ProblemResponses;
import Proconnect.backProconnect.security.AuthRateLimitFilter;
import Proconnect.backProconnect.security.AuthRateLimiter;
import Proconnect.backProconnect.security.ClientIpResolver;
import Proconnect.backProconnect.security.JwtAuthFilter;
import Proconnect.backProconnect.security.OriginCheckFilter;
import Proconnect.backProconnect.web.ProblemCorsProcessor;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthFilter jwtAuthFilter,
            AuthRateLimiter rateLimiter,
            ClientIpResolver ipResolver,
            @Value("${proconnect.cors.origins:http://localhost:4321}") String origins)
            throws Exception {
        http
                // Sesión stateless en cookie HttpOnly + SameSite=Lax; sin mutaciones por GET.
                // El token CSRF de Spring se sustituye por validación de Origin/Referer
                // (OriginCheckFilter).
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> {})
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(
                        auth ->
                                auth.requestMatchers(
                                                HttpMethod.POST,
                                                "/api/auth/login",
                                                "/api/auth/register",
                                                "/api/auth/logout")
                                        .permitAll()
                                        .requestMatchers(HttpMethod.GET, "/actuator/health")
                                        .permitAll()
                                        .requestMatchers("/error")
                                        .permitAll()
                                        .anyRequest()
                                        .authenticated())
                .exceptionHandling(
                        ex ->
                                ex.authenticationEntryPoint(
                                                (req, res, e) ->
                                                        ProblemResponses.write(
                                                                req,
                                                                res,
                                                                HttpStatus.UNAUTHORIZED,
                                                                "Sesión requerida"))
                                        .accessDeniedHandler(
                                                (req, res, e) ->
                                                        ProblemResponses.write(
                                                                req,
                                                                res,
                                                                HttpStatus.FORBIDDEN,
                                                                "Acceso denegado")))
                .addFilterBefore(
                        new OriginCheckFilter(allowedOrigins(origins)),
                        UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(
                        new AuthRateLimitFilter(rateLimiter, ipResolver),
                        UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * El filtro solo corre dentro de la cadena de Spring Security, no como filtro servlet suelto.
     */
    @Bean
    public FilterRegistrationBean<JwtAuthFilter> jwtAuthFilterRegistration(JwtAuthFilter filter) {
        FilterRegistrationBean<JwtAuthFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    /**
     * Spring Security usa el bean llamado {@code corsFilter} en {@code .cors()}: mismo CORS, pero
     * los rechazos en problem+json. Solo corre dentro de la cadena de seguridad (no como filtro
     * servlet suelto).
     */
    @Bean
    public CorsFilter corsFilter(CorsConfigurationSource corsConfigurationSource) {
        CorsFilter filter = new CorsFilter(corsConfigurationSource);
        filter.setCorsProcessor(new ProblemCorsProcessor());
        return filter;
    }

    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilterRegistration(CorsFilter filter) {
        FilterRegistrationBean<CorsFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${proconnect.cors.origins:http://localhost:4321}") String origins) {
        CorsConfiguration config = new CorsConfiguration();
        List<String> allowed =
                Arrays.stream(origins.split(","))
                        .map(String::trim)
                        .filter(o -> !o.isEmpty() && !o.equals("*"))
                        .toList();
        config.setAllowedOrigins(allowed);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    static Set<String> allowedOrigins(String origins) {
        return Arrays.stream(origins.split(","))
                .map(String::trim)
                .filter(o -> !o.isEmpty() && !o.equals("*"))
                .map(o -> o.replaceAll("/+$", "").toLowerCase())
                .collect(Collectors.toUnmodifiableSet());
    }
}
