package com.trading.notification.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Without this class, spring-boot-starter-security on the classpath falls
 * back to Spring Boot's default auto-configuration: it generates a random
 * password at startup and protects every endpoint -- including the /ws
 * SockJS handshake and HTTP-polling fallback -- with HTTP Basic auth. That
 * default surfaces as a browser Basic-Auth popup whenever a request is
 * proxied through the gateway to this service.
 *
 * Real-time auth for the STOMP CONNECT frame is handled separately by
 * WebSocketAuthInterceptor. This class only needs to stop Spring Security's
 * default HTTP Basic layer from intercepting requests before they ever
 * reach the WebSocket handshake or the REST controllers.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/**", "/ws/**").permitAll()
                .anyRequest().permitAll()
            );
        return http.build();
    }
}
