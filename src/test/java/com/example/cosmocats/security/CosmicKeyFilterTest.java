package com.example.cosmocats.security;

import com.example.cosmocats.config.security.CosmicKeyFilter;
import com.example.cosmocats.exception.ApiKeyException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CosmicKeyFilterTest {

    @Mock
    private HttpServletRequest mockRequest;
    
    @Mock
    private HttpServletResponse mockResponse;
    
    @Mock
    private FilterChain mockFilterChain;
    
    private CosmicKeyFilter filter;
    private Method doFilterInternalMethod;

    @BeforeEach
    void setUp() throws Exception {
        filter = new CosmicKeyFilter();
        
        doFilterInternalMethod = CosmicKeyFilter.class
            .getDeclaredMethod("doFilterInternal", 
                HttpServletRequest.class, 
                HttpServletResponse.class, 
                FilterChain.class);
        doFilterInternalMethod.setAccessible(true);
        
        Field validApiKeysField = CosmicKeyFilter.class.getDeclaredField("validApiKeys");
        Field apiKeyHeaderField = CosmicKeyFilter.class.getDeclaredField("apiKeyHeader");
        
        validApiKeysField.setAccessible(true);
        apiKeyHeaderField.setAccessible(true);
        
        validApiKeysField.set(filter, List.of("cosmo-key-123", "admin-key-456"));
        apiKeyHeaderField.set(filter, "X-Cosmo-Key");
        
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_WithValidApiKey_ShouldSetAuthentication() throws Exception {
        when(mockRequest.getHeader("X-Cosmo-Key")).thenReturn("cosmo-key-123");
        
        doFilterInternalMethod.invoke(filter, mockRequest, mockResponse, mockFilterChain);
        
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.isAuthenticated()).isTrue();
        assertThat(authentication.getName()).isEqualTo("api-client");
        
        verify(mockFilterChain).doFilter(mockRequest, mockResponse);
    }

    @Test
    void doFilterInternal_WithInvalidApiKey_ShouldThrowApiKeyException() throws Exception {
        when(mockRequest.getHeader("X-Cosmo-Key")).thenReturn("invalid-key");
        
        assertThatThrownBy(() -> 
            doFilterInternalMethod.invoke(filter, mockRequest, mockResponse, mockFilterChain)
        ).hasCauseInstanceOf(ApiKeyException.class)
         .hasMessageContaining("Invalid API Key: invalid-key");
        
        verify(mockFilterChain, never()).doFilter(any(), any());
    }

    @Test
    void doFilterInternal_WithoutApiKey_ShouldThrowApiKeyException() throws Exception {
        when(mockRequest.getHeader("X-Cosmo-Key")).thenReturn(null);
        
        assertThatThrownBy(() -> 
            doFilterInternalMethod.invoke(filter, mockRequest, mockResponse, mockFilterChain)
        ).hasCauseInstanceOf(ApiKeyException.class)
         .hasMessageContaining("Missing API Key in header: X-Cosmo-Key");
        
        verify(mockFilterChain, never()).doFilter(any(), any());
    }
}