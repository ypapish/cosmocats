package com.example.cosmocats.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.cosmocats.dto.product.ProductDto;
import com.example.cosmocats.dto.product.ProductUpdateDto;
import com.example.cosmocats.exception.ProductAlreadyExistsException;
import com.example.cosmocats.exception.ProductNotFoundException;
import com.example.cosmocats.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminProductController.class)
class AdminProductControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private ProductService productService;

  @Autowired private ObjectMapper objectMapper;

  private ProductDto testProductDto;
  private ProductUpdateDto testProductUpdateDto;
  private UUID productId;

  @BeforeEach
  void setUp() {
    productId = UUID.randomUUID();

    testProductDto =
        ProductDto.builder()
            .productId(productId)
            .category("Electronics")
            .name("Quantum Star Phone")
            .description("Advanced smartphone with quantum processor")
            .price(999.99f)
            .build();

    testProductUpdateDto =
        ProductUpdateDto.builder()
            .category("Electronics")
            .name("Quantum Star Phone")
            .description("Advanced smartphone with quantum processor")
            .price(999.99f)
            .build();
  }

  @Test
  @SneakyThrows
  @WithMockUser(roles = {"ADMIN"})
  void createProduct_WithValidData_ShouldReturnCreatedProduct() {
    Mockito.when(productService.createProduct(Mockito.any(ProductUpdateDto.class)))
        .thenReturn(testProductDto);

    mockMvc
        .perform(
            post("/api/v1/admin/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testProductUpdateDto)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.productId").value(productId.toString()))
        .andExpect(jsonPath("$.name").value("Quantum Star Phone"))
        .andExpect(jsonPath("$.category").value("Electronics"))
        .andExpect(jsonPath("$.price").value(999.99));
  }

  @Test
  @SneakyThrows
  @WithMockUser(roles = {"ADMIN"})
  void createProduct_WithInvalidData_ShouldReturnBadRequest() {
    ProductUpdateDto invalidProductDto =
        ProductUpdateDto.builder()
            .category("Electronics")
            .name("Invalid Product")
            .description("Product with invalid price")
            .price(-10.0f)
            .build();

    mockMvc
        .perform(
            post("/api/v1/admin/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidProductDto)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @SneakyThrows
  @WithMockUser(roles = {"ADMIN"})
  void createProduct_WithInvalidCosmicName_ShouldReturnBadRequest() {
    ProductUpdateDto invalidProductDto =
        ProductUpdateDto.builder()
            .category("Electronics")
            .name("Regular Phone")
            .description("Regular phone description")
            .price(100.0f)
            .build();

    mockMvc
        .perform(
            post("/api/v1/admin/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidProductDto)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @SneakyThrows
  @WithMockUser(roles = {"ADMIN"})
  void createProduct_WithDuplicateName_ShouldReturnConflict() {
    Mockito.when(productService.createProduct(Mockito.any(ProductUpdateDto.class)))
        .thenThrow(new ProductAlreadyExistsException("Galaxy Phone"));

    mockMvc
        .perform(
            post("/api/v1/admin/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testProductUpdateDto)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.title").value("Product Already Exists"));
  }

  @Test
  @SneakyThrows
  @WithMockUser(roles = {"ADMIN"})
  void updateProduct_WithValidData_ShouldReturnUpdatedProduct() {
    ProductUpdateDto updateDto =
        ProductUpdateDto.builder()
            .category("Electronics")
            .name("Updated Galaxy Phone")
            .description("Updated description")
            .price(799.99f)
            .build();

    ProductDto updatedProductDto =
        ProductDto.builder()
            .productId(productId)
            .category("Electronics")
            .name("Updated Galaxy Phone")
            .description("Updated description")
            .price(799.99f)
            .build();

    Mockito.when(
            productService.updateProduct(
                Mockito.eq(productId), Mockito.any(ProductUpdateDto.class)))
        .thenReturn(updatedProductDto);

    mockMvc
        .perform(
            put("/api/v1/admin/products/{id}", productId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Updated Galaxy Phone"))
        .andExpect(jsonPath("$.price").value(799.99))
        .andExpect(jsonPath("$.productId").value(productId.toString()));
  }

  @Test
  @SneakyThrows
  @WithMockUser(roles = {"ADMIN"})
  void updateProduct_WithNonExistingId_ShouldReturnNotFound() {
    UUID nonExistingId = UUID.randomUUID();
    Mockito.when(
            productService.updateProduct(
                Mockito.eq(nonExistingId), Mockito.any(ProductUpdateDto.class)))
        .thenThrow(new ProductNotFoundException(nonExistingId));

    mockMvc
        .perform(
            put("/api/v1/admin/products/{id}", nonExistingId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testProductUpdateDto)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.title").value("Product Not Found"));
  }

  @Test
  @SneakyThrows
  @WithMockUser(roles = {"ADMIN"})
  void updateProduct_WithInvalidData_ShouldReturnBadRequest() {
    ProductUpdateDto invalidUpdateDto =
        ProductUpdateDto.builder()
            .category("")
            .name("")
            .description("Description")
            .price(0.0f)
            .build();

    mockMvc
        .perform(
            put("/api/v1/admin/products/{id}", productId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUpdateDto)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @SneakyThrows
  @WithMockUser(roles = {"ADMIN"})
  void deleteProduct_WithValidId_ShouldReturnNoContent() {
    Mockito.doNothing().when(productService).deleteProduct(productId);

    mockMvc
        .perform(delete("/api/v1/admin/products/{id}", productId))
        .andExpect(status().isNoContent());

    Mockito.verify(productService).deleteProduct(productId);
  }

  @Test
  @SneakyThrows
  @WithMockUser(roles = {"ADMIN"})
  void deleteProduct_WithNonExistingId_ShouldReturnNotFound() {
    UUID nonExistingId = UUID.randomUUID();
    Mockito.doThrow(new ProductNotFoundException(nonExistingId))
        .when(productService)
        .deleteProduct(nonExistingId);

    mockMvc
        .perform(delete("/api/v1/admin/products/{id}", nonExistingId))
        .andExpect(status().isNotFound());

    Mockito.verify(productService).deleteProduct(nonExistingId);
  }

  @Test
  @SneakyThrows
  @WithMockUser(roles = {"ADMIN"})
  void createProduct_WithMissingRequiredFields_ShouldReturnBadRequest() {
    ProductUpdateDto invalidProductDto =
        ProductUpdateDto.builder()
            .category(null)
            .name(null)
            .description("Description")
            .price(null)
            .build();

    mockMvc
        .perform(
            post("/api/v1/admin/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidProductDto)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @SneakyThrows
  @WithMockUser(roles = {"USER"})
  void adminEndpoint_WithUserRole_ShouldReturnForbidden() {
    mockMvc
        .perform(
            post("/api/v1/admin/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testProductUpdateDto)))
        .andExpect(status().isForbidden());
  }

  @Test
  @SneakyThrows
  void adminEndpoint_WithoutAuthentication_ShouldReturnUnauthorized() {
    mockMvc
        .perform(
            post("/api/v1/admin/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testProductUpdateDto)))
        .andExpect(status().isUnauthorized());
  }
}
