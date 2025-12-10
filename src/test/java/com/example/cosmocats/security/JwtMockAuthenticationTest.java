package com.example.cosmocats.security;

import com.example.cosmocats.AbstractIt;
import com.example.cosmocats.dto.product.ProductUpdateDto;
import com.example.cosmocats.entity.CategoryEntity;
import com.example.cosmocats.repository.CategoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class JwtMockAuthenticationTest extends AbstractIt {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        categoryRepository.deleteAll();
        
        CategoryEntity category = CategoryEntity.builder()
                .categoryUuid(UUID.randomUUID())
                .name("Electronics")
                .build();
        categoryRepository.save(category);
    }

    @Test
    @SneakyThrows
    void testJwtAuthentication_WithAdminRole_ShouldAccessAdminEndpoints() {
        ProductUpdateDto createDto = ProductUpdateDto.builder()
                .category("Electronics")
                .name("JWT Admin Product")
                .description("Created by admin via JWT")
                .price(999.99f)
                .build();

        mockMvc.perform(post("/api/v1/admin/products")
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .subject("admin-user")
                                        .claim("authorities", List.of("ADMIN"))
                                        .issuedAt(Instant.now())
                                        .expiresAt(Instant.now().plusSeconds(3600)))
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated());
    }

    @Test
    @SneakyThrows
    void testJwtAuthentication_WithUserRole_ShouldAccessUserEndpoints() {
        mockMvc.perform(post("/api/v1/orders")
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .subject("regular-user")
                                        .claim("authorities", List.of("USER"))
                                        .issuedAt(Instant.now())
                                        .expiresAt(Instant.now().plusSeconds(3600)))
                        )
                        .content("{\"totalPrice\": 100.0, \"entries\": []}")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void testJwtAuthentication_UserRole_CannotAccessAdminEndpoints() {
        ProductUpdateDto createDto = ProductUpdateDto.builder()
                .category("Electronics")
                .name("User Tries Admin")
                .description("Should be forbidden")
                .price(100.0f)
                .build();

        mockMvc.perform(post("/api/v1/admin/products")
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .subject("regular-user")
                                        .claim("authorities", List.of("USER"))
                                        .issuedAt(Instant.now())
                                        .expiresAt(Instant.now().plusSeconds(3600)))
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void testJwtAuthentication_WithInvalidClaims_ShouldWorkWithAuthorityConverter() {
        mockMvc.perform(get("/api/v1/categories")
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .subject("test-user")
                                        .claim("authorities", List.of("ADMIN", "USER"))
                                        .issuedAt(Instant.now())
                                        .expiresAt(Instant.now().plusSeconds(3600)))
                        ))
                .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    void testJwtAuthentication_WithoutBearerToken_ShouldReturnUnauthorized() {
        ProductUpdateDto createDto = ProductUpdateDto.builder()
                .category("Electronics")
                .name("No Auth Product")
                .description("Should fail without token")
                .price(100.0f)
                .build();

        mockMvc.perform(post("/api/v1/admin/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @SneakyThrows
    void testJwtAuthentication_WithCustomClaims_ShouldWork() {
        mockMvc.perform(get("/api/v1/categories")
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .subject("custom-user")
                                        .claim("preferred_username", "john.doe")
                                        .claim("email", "john@example.com")
                                        .claim("authorities", List.of("USER"))
                                        .issuedAt(Instant.now())
                                        .expiresAt(Instant.now().plusSeconds(3600)))
                        ))
                .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    void testJwtAuthentication_AdminCanAccessAllOrders() {
        mockMvc.perform(get("/api/v1/orders")
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .subject("admin-user")
                                        .claim("authorities", List.of("ADMIN"))
                                        .issuedAt(Instant.now())
                                        .expiresAt(Instant.now().plusSeconds(3600)))
                        ))
                .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    void testJwtAuthentication_UserCannotAccessAllOrders() {
        mockMvc.perform(get("/api/v1/orders")
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .subject("regular-user")
                                        .claim("authorities", List.of("USER"))
                                        .issuedAt(Instant.now())
                                        .expiresAt(Instant.now().plusSeconds(3600)))
                        ))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void testJwtAuthentication_CreateCategory_RequiresAdmin() {
        mockMvc.perform(post("/api/v1/categories")
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .subject("admin-user")
                                        .claim("authorities", List.of("ADMIN"))
                                        .issuedAt(Instant.now())
                                        .expiresAt(Instant.now().plusSeconds(3600)))
                        )
                        .content("{\"name\": \"Test Category\"}")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    @SneakyThrows
    void testJwtAuthentication_GetCategories_AllowedForAll() {
        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    void testAuthorityConverter_WithDifferentClaimNames() {
        mockMvc.perform(get("/api/v1/categories")
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .subject("test-user")
                                        .claim("roles", List.of("ADMIN"))
                                        .issuedAt(Instant.now())
                                        .expiresAt(Instant.now().plusSeconds(3600)))
                        ))
                .andExpect(status().isOk());
    }
}