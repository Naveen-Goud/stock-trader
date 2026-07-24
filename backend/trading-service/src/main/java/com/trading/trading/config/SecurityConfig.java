package com.trading.trading.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Without this class, spring-boot-starter-security on the classpath falls
 * back to Spring Boot's default auto-configuration: it generates a random
 * password at startup and protects every endpoint with HTTP Basic auth.
 * That default surfaces as a browser Basic-Auth popup whenever a request is
 * proxied through the gateway to this service.
 *
 * Authentication/authorization is enforced once, at the API Gateway
 * (JwtGatewayFilter), which injects X-User-Id / X-User-Role headers after
 * validating the JWT. This service trusts the gateway as its authentication
 * boundary and simply disables the default security so those headers pass
 * through unobstructed.
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
                .requestMatchers("/actuator/**").permitAll()
                .anyRequest().permitAll()
            );
        return http.build();
    }
}
