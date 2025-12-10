package com.example.cosmocats.config.security;

import com.example.cosmocats.exception.ApiKeyException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
public class CosmicKeyFilter extends OncePerRequestFilter {

    @Value("#{'${application.security.api-key.valid-keys:cosmo-key-123}'.split(',')}")
    private List<String> validApiKeys;

    @Value("${application.security.api-key.header-name:X-Cosmo-Key}")
    private String apiKeyHeader;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) 
            throws ServletException, IOException {
        
        String apiKey = request.getHeader(apiKeyHeader);
        
        if (apiKey == null || apiKey.isBlank()) {
            throw new ApiKeyException("Missing API Key in header: " + apiKeyHeader);
        }
        
        log.debug("Validating API Key: {}", apiKey);
        String trimmedApiKey = apiKey.trim();
        
        if (!validApiKeys.contains(trimmedApiKey)) {
            throw new ApiKeyException("Invalid API Key: " + apiKey);
        }
        
        var authentication = createAuthentication(trimmedApiKey, request);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        log.debug("API Key authentication successful");
        filterChain.doFilter(request, response);
    }
    
    private UsernamePasswordAuthenticationToken createAuthentication(
            String apiKey, HttpServletRequest request) {
        
        List<SimpleGrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority("ROLE_ADMIN"),
            new SimpleGrantedAuthority("ROLE_API")
        );
        
        UsernamePasswordAuthenticationToken authentication = 
            new UsernamePasswordAuthenticationToken("api-client", apiKey, authorities);
        
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        return authentication;
    }
}