package com.example.cosmocats.config.noauth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Slf4j
@Profile("no-auth")
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(NoAuthProperties.class)
public class NoAuthSecurityConfiguration {

    @Bean
    @Order(Integer.MIN_VALUE)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.warn("Running in no-auth mode - all requests are permitted");
        http
                .csrf(CsrfConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

    @Bean
    @Order(Integer.MIN_VALUE)
    public WebSecurityCustomizer webSecurityCustomizer() {
        log.warn("WebSecurity ignoring all requests");
        return WebSecurity::ignoring;
    }

    @Bean
    public OncePerRequestFilter authenticationFilter(NoAuthProperties noAuthProperties) {
        return new AuthenticationFilter(noAuthProperties.getClaims());
    }

    private static class AuthenticationFilter extends OncePerRequestFilter {
        private final Map<String, Object> claims;

        public AuthenticationFilter(Map<String, Object> claims) {
            this.claims = claims;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                        FilterChain filterChain) throws ServletException, IOException {
            Jwt jwt = Jwt.withTokenValue("mock-token-no-auth")
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plus(Duration.ofMinutes(3)))
                    .header("alg", "HS256")
                    .header("kid", "cosmo-key")
                    .subject("test-user")
                    .claim("roles", List.of("ADMIN", "USER"))
                    .claims((headers) -> headers.putAll(claims))
                    .build();

            JwtAuthenticationToken jwtToken = new JwtAuthenticationToken(
                    jwt,
                    List.of(new SimpleGrantedAuthority("ROLE_ADMIN"),
                            new SimpleGrantedAuthority("ROLE_USER"))
            );

            SecurityContext context = SecurityContextHolder.getContext();
            context.setAuthentication(jwtToken);

            filterChain.doFilter(request, response);
        }
    }
}