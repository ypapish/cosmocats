package com.example.cosmocats.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.cosmocats.AbstractIt;
import com.example.cosmocats.dto.product.ProductUpdateDto;
import com.example.cosmocats.entity.CategoryEntity;
import com.example.cosmocats.repository.CategoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
class ApiKeyIntegrationTest extends AbstractIt {

  @Autowired private MockMvc mockMvc;

  @Autowired private CategoryRepository categoryRepository;

  @Autowired private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    categoryRepository.deleteAll();

    CategoryEntity category =
        CategoryEntity.builder().categoryUuid(UUID.randomUUID()).name("Electronics").build();
    categoryRepository.save(category);
  }

  @Test
  @SneakyThrows
  void testApiKeyAuthentication_ValidKey_ShouldAllowAdminAccess() {
    ProductUpdateDto createDto =
        ProductUpdateDto.builder()
            .category("Electronics")
            .name("Galaxy Quantum Phone")
            .description("Advanced quantum smartphone")
            .price(1299.99f)
            .build();

    mockMvc
        .perform(
            post("/api/v1/admin/products")
                .header("X-Cosmo-Key", "cosmo-key-123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
        .andExpect(status().isCreated());
  }

  @Test
  @SneakyThrows
  void testApiKeyAuthentication_InvalidKey_ShouldReturnUnauthorized() {
    ProductUpdateDto createDto =
        ProductUpdateDto.builder()
            .category("Electronics")
            .name("Unauthorized Product")
            .description("Should fail")
            .price(100.0f)
            .build();

    mockMvc
        .perform(
            post("/api/v1/admin/products")
                .header("X-Cosmo-Key", "wrong-key-789")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.message").value("Invalid API Key: wrong-key-789"));
  }

  @Test
  @SneakyThrows
  void testApiKeyAuthentication_MissingKey_ShouldReturnUnauthorized() {
    ProductUpdateDto createDto =
        ProductUpdateDto.builder()
            .category("Electronics")
            .name("No Key Product")
            .description("Should fail without key")
            .price(100.0f)
            .build();

    mockMvc
        .perform(
            post("/api/v1/admin/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.message").value("Missing API Key in header: X-Cosmo-Key"));
  }
}
