package com.example.cosmocats.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.cosmocats.AbstractIt;
import com.example.cosmocats.dto.product.ProductUpdateDto;
import com.example.cosmocats.entity.CategoryEntity;
import com.example.cosmocats.entity.ProductEntity;
import com.example.cosmocats.repository.CategoryRepository;
import com.example.cosmocats.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
class AdminProductControllerIntegrationTest extends AbstractIt {

  @Autowired 
  private MockMvc mockMvc;

  @Autowired 
  private ProductRepository productRepository;

  @Autowired 
  private CategoryRepository categoryRepository;

  @Autowired 
  private ObjectMapper objectMapper;

  private CategoryEntity testCategory;

  @BeforeEach
  void setUp() {
    productRepository.deleteAll();
    categoryRepository.deleteAll();

    testCategory =
        CategoryEntity.builder().categoryUuid(UUID.randomUUID()).name("Electronics").build();
    categoryRepository.save(testCategory);
  }

  @Test
  @SneakyThrows
  @WithMockUser(roles = {"ADMIN"})
  void createProduct_WithValidData_ShouldCreateProduct() {
    ProductUpdateDto createDto =
        ProductUpdateDto.builder()
            .category("Electronics")
            .name("Quantum Star Phone")
            .description("Advanced smartphone with quantum processor")
            .price(999.99f)
            .build();

    mockMvc
        .perform(
            post("/api/v1/admin/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name").value("Quantum Star Phone"))
        .andExpect(jsonPath("$.category").value("Electronics"))
        .andExpect(jsonPath("$.price").value(999.99))
        .andExpect(jsonPath("$.productId").exists());

    assertEquals(1, productRepository.count());
    assertTrue(productRepository.findByName("Quantum Star Phone").isPresent());
  }

  @Test
  @SneakyThrows
  @WithMockUser(roles = {"ADMIN"})
  void createProduct_WithDuplicateName_ShouldReturnConflict() {
    ProductEntity existingProduct =
        ProductEntity.builder()
            .productUuid(UUID.randomUUID())
            .category(testCategory)
            .name("Galaxy Tablet")
            .description("Existing product")
            .price(BigDecimal.valueOf(499.99))
            .build();
    productRepository.save(existingProduct);

    ProductUpdateDto duplicateProduct =
        ProductUpdateDto.builder()
            .category("Electronics")
            .name("Galaxy Tablet")
            .description("New product with same name")
            .price(599.99f)
            .build();

    mockMvc
        .perform(
            post("/api/v1/admin/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateProduct)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.title").value("Product Already Exists"));

    assertEquals(1, productRepository.count());
  }

  @Test
  @SneakyThrows
  @WithMockUser(roles = {"ADMIN"})
  void createProduct_WithNonExistingCategory_ShouldReturnBadRequest() {
    ProductUpdateDto productDto =
        ProductUpdateDto.builder()
            .category("NonExistingCategory")
            .name("Cosmic Device")
            .description("Test product")
            .price(100.0f)
            .build();

    mockMvc
        .perform(
            post("/api/v1/admin/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productDto)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @SneakyThrows
  @WithMockUser(roles = {"ADMIN"})
  void createProduct_WithInvalidCosmicName_ShouldReturnBadRequest() {
    ProductUpdateDto invalidProduct =
        ProductUpdateDto.builder()
            .category("Electronics")
            .name("Regular Phone")
            .description("Regular phone without cosmic terms")
            .price(100.0f)
            .build();

    mockMvc
        .perform(
            post("/api/v1/admin/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidProduct)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @SneakyThrows
  @WithMockUser(roles = {"ADMIN"})
  void updateProduct_WithValidData_ShouldUpdateProduct() {
    ProductEntity existingProduct =
        ProductEntity.builder()
            .productUuid(UUID.randomUUID())
            .category(testCategory)
            .name("Old Star Phone")
            .description("Old description")
            .price(BigDecimal.valueOf(500.0))
            .build();
    ProductEntity savedProduct = productRepository.save(existingProduct);

    ProductUpdateDto updateDto =
        ProductUpdateDto.builder()
            .category("Electronics")
            .name("Updated Galaxy Phone")
            .description("Updated description")
            .price(799.99f)
            .build();

    mockMvc
        .perform(
            put("/api/v1/admin/products/{id}", savedProduct.getProductUuid())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Updated Galaxy Phone"))
        .andExpect(jsonPath("$.price").value(799.99));

    ProductEntity updatedProduct =
        productRepository.findByNaturalId(savedProduct.getProductUuid()).get();
    assertEquals("Updated Galaxy Phone", updatedProduct.getName());
    assertEquals(BigDecimal.valueOf(799.99), updatedProduct.getPrice());
  }

  @Test
  @SneakyThrows
  @WithMockUser(roles = {"ADMIN"})
  void updateProduct_WithNonExistingId_ShouldReturnNotFound() {
    UUID nonExistingId = UUID.randomUUID();
    ProductUpdateDto updateDto =
        ProductUpdateDto.builder()
            .category("Electronics")
            .name("Updated Product")
            .description("Updated description")
            .price(100.0f)
            .build();

    mockMvc
        .perform(
            put("/api/v1/admin/products/{id}", nonExistingId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
        .andExpect(status().isNotFound());
  }

  @Test
  @SneakyThrows
  @WithMockUser(roles = {"ADMIN"})
  void updateProduct_WithDuplicateName_ShouldReturnConflict() {
    ProductEntity product1 =
        ProductEntity.builder()
            .productUuid(UUID.randomUUID())
            .category(testCategory)
            .name("First Product")
            .description("First product description")
            .price(BigDecimal.valueOf(100.0))
            .build();
    ProductEntity product2 =
        ProductEntity.builder()
            .productUuid(UUID.randomUUID())
            .category(testCategory)
            .name("Second Product")
            .description("Second product description")
            .price(BigDecimal.valueOf(200.0))
            .build();
    productRepository.save(product1);
    ProductEntity savedProduct2 = productRepository.save(product2);

    ProductUpdateDto updateDto =
        ProductUpdateDto.builder()
            .category("Electronics")
            .name("First Product")
            .description("Updated description")
            .price(150.0f)
            .build();

    mockMvc
        .perform(
            put("/api/v1/admin/products/{id}", savedProduct2.getProductUuid())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
        .andExpect(status().isConflict());
  }

  @Test
  @SneakyThrows
  @WithMockUser(roles = {"ADMIN"})
  void deleteProduct_WithExistingId_ShouldDeleteProduct() {
    ProductEntity product =
        ProductEntity.builder()
            .productUuid(UUID.randomUUID())
            .category(testCategory)
            .name("Product To Delete")
            .description("Product description")
            .price(BigDecimal.valueOf(300.0))
            .build();
    ProductEntity savedProduct = productRepository.save(product);

    mockMvc
        .perform(delete("/api/v1/admin/products/{id}", savedProduct.getProductUuid()))
        .andExpect(status().isNoContent());

    assertFalse(productRepository.findByNaturalId(savedProduct.getProductUuid()).isPresent());
  }

  @Test
  @SneakyThrows
  @WithMockUser(roles = {"ADMIN"})
  void deleteProduct_WithNonExistingId_ShouldReturnNotFound() {
    UUID nonExistingId = UUID.randomUUID();

    mockMvc
        .perform(delete("/api/v1/admin/products/{id}", nonExistingId))
        .andExpect(status().isNotFound());
  }
}
