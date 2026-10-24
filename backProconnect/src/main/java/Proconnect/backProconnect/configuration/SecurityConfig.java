package Proconnect.backProconnect.configuration;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

import Proconnect.backProconnect.security.JwtAuthFilter;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class SecurityConfig {

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder(12);
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
                http
                        // Sesión stateless en cookie HttpOnly + SameSite=Lax; sin mutaciones por GET.
                        // La protección CSRF adicional se revisa en verify-web-security.
                        .csrf(AbstractHttpConfigurer::disable)
                        .cors(cors -> {})
                        .formLogin(AbstractHttpConfigurer::disable)
                        .httpBasic(AbstractHttpConfigurer::disable)
                        .logout(AbstractHttpConfigurer::disable)
                        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                        .authorizeHttpRequests(auth -> auth
                                .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/register",
                                                "/api/auth/logout").permitAll()
                                .requestMatchers(HttpMethod.GET, "/actuator/health").permitAll()
                                .requestMatchers("/error").permitAll()
                                .anyRequest().authenticated())
                        .exceptionHandling(ex -> ex
                                .authenticationEntryPoint((req, res, e) -> writeJson(res, HttpStatus.UNAUTHORIZED,
                                                "Sesión requerida"))
                                .accessDeniedHandler((req, res, e) -> writeJson(res, HttpStatus.FORBIDDEN,
                                                "Acceso denegado")))
                        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
                return http.build();
        }

        /** El filtro solo corre dentro de la cadena de Spring Security, no como filtro servlet suelto. */
        @Bean
        public FilterRegistrationBean<JwtAuthFilter> jwtAuthFilterRegistration(JwtAuthFilter filter) {
                FilterRegistrationBean<JwtAuthFilter> registration = new FilterRegistrationBean<>(filter);
                registration.setEnabled(false);
                return registration;
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource(
                        @Value("${proconnect.cors.origins:http://localhost:4321}") String origins) {
                CorsConfiguration config = new CorsConfiguration();
                List<String> allowed = Arrays.stream(origins.split(",")).map(String::trim)
                                .filter(o -> !o.isEmpty() && !o.equals("*")).toList();
                config.setAllowedOrigins(allowed);
                config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                config.setAllowedHeaders(List.of("*"));
                config.setAllowCredentials(true);
                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", config);
                return source;
        }

        private static void writeJson(HttpServletResponse res, HttpStatus status, String message)
                        throws java.io.IOException {
                res.setStatus(status.value());
                res.setContentType(MediaType.APPLICATION_JSON_VALUE);
                res.setCharacterEncoding("UTF-8");
                res.getWriter().write("{\"status\":" + status.value() + ",\"error\":\"" + status.getReasonPhrase()
                                + "\",\"message\":\"" + message + "\"}");
        }
}
