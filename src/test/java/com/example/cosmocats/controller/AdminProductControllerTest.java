package com.example.cosmocats.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.cosmocats.dto.product.ProductDto;
import com.example.cosmocats.dto.product.ProductUpdateDto;
import com.example.cosmocats.exception.ProductAlreadyExistsException;
import com.example.cosmocats.exception.ProductNotFoundException;
import com.example.cosmocats.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminProductController.class)
@DisplayName("Admin Product Controller Tests")
class AdminProductControllerTest {

  private final UUID productId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
  
  @Autowired 
  private MockMvc mockMvc;

  @Autowired 
  private ObjectMapper objectMapper;
  
  @MockitoBean 
  private ProductService productService;

  @Test
  @DisplayName("Should create product with valid data")
  void createProduct_ShouldReturnCreated_WhenValidData() throws Exception {
    ProductUpdateDto createDto =
        ProductUpdateDto.builder()
            .category("Electronics")
            .name("Galaxy Phone")
            .description("Advanced smartphone with cosmic features")
            .price(999.99f)
            .build();

    ProductDto responseDto =
        ProductDto.builder()
            .productId(productId)
            .category("Electronics")
            .name("Galaxy Phone")
            .description("Advanced smartphone with cosmic features")
            .price(999.99f)
            .build();

    when(productService.createProduct(any(ProductUpdateDto.class))).thenReturn(responseDto);

    mockMvc
        .perform(
            post("/api/v1/admin/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.productId").value(productId.toString()))
        .andExpect(jsonPath("$.name").value("Galaxy Phone"))
        .andExpect(jsonPath("$.category").value("Electronics"))
        .andExpect(jsonPath("$.description").value("Advanced smartphone with cosmic features"))
        .andExpect(jsonPath("$.price").value(999.99));

    verify(productService).createProduct(any(ProductUpdateDto.class));
  }

  @Test
  @DisplayName("Should update product with valid data")
  void updateProduct_ShouldReturnOk_WhenValidData() throws Exception {
    ProductUpdateDto updateDto =
        ProductUpdateDto.builder()
            .category("Electronics")
            .name("Updated Galaxy Phone")
            .description("Updated description with cosmic enhancements")
            .price(899.99f)
            .build();

    ProductDto responseDto =
        ProductDto.builder()
            .productId(productId)
            .category("Electronics")
            .name("Updated Galaxy Phone")
            .description("Updated description with cosmic enhancements")
            .price(899.99f)
            .build();

    when(productService.updateProduct(eq(productId), any(ProductUpdateDto.class)))
        .thenReturn(responseDto);

    mockMvc
        .perform(
            put("/api/v1/admin/products/{id}", productId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.productId").value(productId.toString()))
        .andExpect(jsonPath("$.name").value("Updated Galaxy Phone"))
        .andExpect(jsonPath("$.category").value("Electronics"))
        .andExpect(jsonPath("$.description").value("Updated description with cosmic enhancements"))
        .andExpect(jsonPath("$.price").value(899.99));

    verify(productService).updateProduct(eq(productId), any(ProductUpdateDto.class));
  }

  @Test
  @DisplayName("Should delete product when product exists")
  void deleteProduct_ShouldReturnNoContent_WhenProductExists() throws Exception {
    doNothing().when(productService).deleteProduct(productId);

    mockMvc
        .perform(delete("/api/v1/admin/products/{id}", productId))
        .andExpect(status().isNoContent());

    verify(productService).deleteProduct(productId);
  }

  @Test
  @DisplayName("Should return bad request when category is blank")
  void createProduct_ShouldReturnBadRequest_WhenCategoryIsBlank() throws Exception {
    ProductUpdateDto invalidDto =
        ProductUpdateDto.builder()
            .category("")
            .name("Galaxy Phone")
            .description("Advanced smartphone")
            .price(999.99f)
            .build();

    mockMvc
        .perform(
            post("/api/v1/admin/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
        .andExpect(status().isBadRequest());

    verify(productService, never()).createProduct(any(ProductUpdateDto.class));
  }

  @Test
  @DisplayName("Should return bad request when name is blank")
  void createProduct_ShouldReturnBadRequest_WhenNameIsBlank() throws Exception {
    ProductUpdateDto invalidDto =
        ProductUpdateDto.builder()
            .category("Electronics")
            .name("") // Blank name
            .description("Advanced smartphone")
            .price(999.99f)
            .build();

    mockMvc
        .perform(
            post("/api/v1/admin/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
        .andExpect(status().isBadRequest());

    verify(productService, never()).createProduct(any(ProductUpdateDto.class));
  }

  @Test
  @DisplayName("Should return bad request when price is zero")
  void createProduct_ShouldReturnBadRequest_WhenPriceIsZero() throws Exception {
    ProductUpdateDto invalidDto =
        ProductUpdateDto.builder()
            .category("Electronics")
            .name("Galaxy Phone")
            .description("Advanced smartphone")
            .price(0.0f)
            .build();

    mockMvc
        .perform(
            post("/api/v1/admin/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
        .andExpect(status().isBadRequest());

    verify(productService, never()).createProduct(any(ProductUpdateDto.class));
  }

  @Test
  @DisplayName("Should return bad request when price is negative")
  void createProduct_ShouldReturnBadRequest_WhenPriceIsNegative() throws Exception {
    ProductUpdateDto invalidDto =
        ProductUpdateDto.builder()
            .category("Electronics")
            .name("Galaxy Phone")
            .description("Advanced smartphone")
            .price(-10.0f)
            .build();

    mockMvc
        .perform(
            post("/api/v1/admin/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
        .andExpect(status().isBadRequest());

    verify(productService, never()).createProduct(any(ProductUpdateDto.class));
  }

  @Test
  @DisplayName("Should return bad request when name exceeds max length")
  void createProduct_ShouldReturnBadRequest_WhenNameExceedsMaxLength() throws Exception {
    String longName = "A".repeat(101);
    ProductUpdateDto invalidDto =
        ProductUpdateDto.builder()
            .category("Electronics")
            .name(longName)
            .description("Advanced smartphone")
            .price(999.99f)
            .build();

    mockMvc
        .perform(
            post("/api/v1/admin/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
        .andExpect(status().isBadRequest());

    verify(productService, never()).createProduct(any(ProductUpdateDto.class));
  }

  @Test
  @DisplayName("Should return bad request when description exceeds max length")
  void createProduct_ShouldReturnBadRequest_WhenDescriptionExceedsMaxLength() throws Exception {
    String longDescription = "A".repeat(1001);
    ProductUpdateDto invalidDto =
        ProductUpdateDto.builder()
            .category("Electronics")
            .name("Galaxy Phone")
            .description(longDescription)
            .price(999.99f)
            .build();

    mockMvc
        .perform(
            post("/api/v1/admin/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
        .andExpect(status().isBadRequest());

    verify(productService, never()).createProduct(any(ProductUpdateDto.class));
  }

  @Test
  @DisplayName("Should return bad request when missing cosmic word")
  void createProduct_ShouldReturnBadRequest_WhenMissingCosmicWord() throws Exception {
    ProductUpdateDto invalidDto =
        ProductUpdateDto.builder()
            .category("Electronics")
            .name("Regular Phone")
            .description("Regular smartphone")
            .price(999.99f)
            .build();

    mockMvc
        .perform(
            post("/api/v1/admin/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
        .andExpect(status().isBadRequest());

    verify(productService, never()).createProduct(any(ProductUpdateDto.class));
  }

  @Test
  @DisplayName("Should return conflict when product already exists")
  void createProduct_ShouldReturnConflict_WhenProductAlreadyExists() throws Exception {
    ProductUpdateDto createDto =
        ProductUpdateDto.builder()
            .category("Electronics")
            .name("Galaxy Phone")
            .description("Advanced smartphone")
            .price(999.99f)
            .build();

    when(productService.createProduct(any(ProductUpdateDto.class)))
        .thenThrow(new ProductAlreadyExistsException("Galaxy Phone"));

    mockMvc
        .perform(
            post("/api/v1/admin/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.title").value("Product Already Exists"));

    verify(productService).createProduct(any(ProductUpdateDto.class));
  }

  @Test
  @DisplayName("Should return not found when updating non-existent product")
  void updateProduct_ShouldReturnNotFound_WhenProductNotFound() throws Exception {
    ProductUpdateDto updateDto =
        ProductUpdateDto.builder()
            .category("Electronics")
            .name("Updated Galaxy Phone")
            .description("Updated description")
            .price(899.99f)
            .build();

    when(productService.updateProduct(eq(productId), any(ProductUpdateDto.class)))
        .thenThrow(new ProductNotFoundException(productId));

    mockMvc
        .perform(
            put("/api/v1/admin/products/{id}", productId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.title").value("Product Not Found"));

    verify(productService).updateProduct(eq(productId), any(ProductUpdateDto.class));
  }

  @Test
  @DisplayName("Should return conflict when product name already exists during update")
  void updateProduct_ShouldReturnConflict_WhenProductNameAlreadyExists() throws Exception {
    ProductUpdateDto updateDto =
        ProductUpdateDto.builder()
            .category("Electronics")
            .name("Existing Galaxy Product")
            .description("Updated description")
            .price(899.99f)
            .build();

    when(productService.updateProduct(eq(productId), any(ProductUpdateDto.class)))
        .thenThrow(new ProductAlreadyExistsException("Existing Galaxy Product"));

    mockMvc
        .perform(
            put("/api/v1/admin/products/{id}", productId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.title").value("Product Already Exists"));

    verify(productService).updateProduct(eq(productId), any(ProductUpdateDto.class));
  }
}
