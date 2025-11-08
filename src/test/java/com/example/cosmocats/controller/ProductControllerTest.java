package com.example.cosmocats.controller;

import com.example.cosmocats.dto.product.ProductDto;
import com.example.cosmocats.dto.product.ProductListDto;
import com.example.cosmocats.exception.ProductNotFoundException;
import com.example.cosmocats.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@DisplayName("Product Controller Tests")
class ProductControllerTest {

    private final UUID productId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private ProductService productService;

    @Test
    @DisplayName("Should return all products when products exist")
    void getAllProducts_ShouldReturnProducts_WhenProductsExist() throws Exception {
        ProductDto product1 =
                ProductDto.builder()
                        .productId(productId)
                        .category("Electronics")
                        .name("Galaxy Phone")
                        .description("Advanced smartphone with cosmic design")
                        .price(999.99f)
                        .build();

        ProductDto product2 =
                ProductDto.builder()
                        .productId(UUID.fromString("550e8400-e29b-41d4-a716-446655440002"))
                        .category("Books")
                        .name("Star Guide")
                        .description("Complete guide to space exploration")
                        .price(29.99f)
                        .build();

        List<ProductDto> products = Arrays.asList(product1, product2);
        ProductListDto productListDto = ProductListDto.builder().products(products).build();

        when(productService.getAllProducts()).thenReturn(productListDto);

        mockMvc
                .perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products.length()").value(2))
                .andExpect(jsonPath("$.products[0].productId").value(productId.toString()))
                .andExpect(jsonPath("$.products[0].name").value("Galaxy Phone"))
                .andExpect(jsonPath("$.products[0].category").value("Electronics"))
                .andExpect(
                        jsonPath("$.products[0].description").value("Advanced smartphone with cosmic design"))
                .andExpect(jsonPath("$.products[0].price").value(999.99))
                .andExpect(jsonPath("$.products[1].name").value("Star Guide"))
                .andExpect(jsonPath("$.products[1].category").value("Books"))
                .andExpect(jsonPath("$.products[1].price").value(29.99));
    }

    @Test
    @DisplayName("Should return empty list when no products exist")
    void getAllProducts_ShouldReturnEmptyList_WhenNoProductsExist() throws Exception {
        ProductListDto emptyListDto = ProductListDto.builder().products(List.of()).build();
        when(productService.getAllProducts()).thenReturn(emptyListDto);

        mockMvc
                .perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products.length()").value(0));
    }

    @Test
    @DisplayName("Should return product by ID when product exists")
    void getProductById_ShouldReturnProduct_WhenProductExists() throws Exception {
        ProductDto productDto =
                ProductDto.builder()
                        .productId(productId)
                        .category("Electronics")
                        .name("Galaxy Phone")
                        .description("Advanced smartphone with cosmic design")
                        .price(999.99f)
                        .build();

        when(productService.getProductById(productId)).thenReturn(productDto);

        mockMvc
                .perform(get("/api/v1/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(productId.toString()))
                .andExpect(jsonPath("$.name").value("Galaxy Phone"))
                .andExpect(jsonPath("$.category").value("Electronics"))
                .andExpect(jsonPath("$.description").value("Advanced smartphone with cosmic design"))
                .andExpect(jsonPath("$.price").value(999.99));
    }

    @Test
    @DisplayName("Should return filtered products by category")
    void getProductsByCategory_ShouldReturnFilteredProducts() throws Exception {
        ProductDto electronicsProduct =
                ProductDto.builder()
                        .productId(productId)
                        .category("Electronics")
                        .name("Galaxy Phone")
                        .description("Advanced smartphone with cosmic design")
                        .price(999.99f)
                        .build();

        List<ProductDto> electronicsProducts = List.of(electronicsProduct);
        ProductListDto productListDto = ProductListDto.builder().products(electronicsProducts).build();

        when(productService.getProductsByCategory("Electronics")).thenReturn(productListDto);

        mockMvc
                .perform(get("/api/v1/products/category/{category}", "Electronics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products.length()").value(1))
                .andExpect(jsonPath("$.products[0].productId").value(productId.toString()))
                .andExpect(jsonPath("$.products[0].category").value("Electronics"))
                .andExpect(jsonPath("$.products[0].name").value("Galaxy Phone"))
                .andExpect(
                        jsonPath("$.products[0].description").value("Advanced smartphone with cosmic design"))
                .andExpect(jsonPath("$.products[0].price").value(999.99));
    }

    @Test
    @DisplayName("Should return empty list when no products in category")
    void getProductsByCategory_ShouldReturnEmptyList_WhenNoProductsInCategory() throws Exception {
        ProductListDto emptyListDto = ProductListDto.builder().products(List.of()).build();
        when(productService.getProductsByCategory("NonExistent")).thenReturn(emptyListDto);

        mockMvc
                .perform(get("/api/v1/products/category/{category}", "NonExistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products.length()").value(0));
    }

    @Test
    @DisplayName("Should return not found when product does not exist")
    void getProductById_ShouldReturnNotFound_WhenProductDoesNotExist() throws Exception {
        when(productService.getProductById(productId))
                .thenThrow(new ProductNotFoundException(productId));

        mockMvc
                .perform(get("/api/v1/products/{id}", productId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Product Not Found"));
    }

    @Test
    @DisplayName("Should return bad request for invalid UUID")
    void getProductById_ShouldReturnBadRequest_WhenInvalidUUID() throws Exception {
        String invalidUuid = "invalid-uuid";

        mockMvc
                .perform(get("/api/v1/products/{id}", invalidUuid))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid Parameter"));
    }

    @Test
    @DisplayName("Should handle case insensitive categories")
    void getProductsByCategory_ShouldHandleCaseInsensitiveCategories() throws Exception {
        ProductDto product =
                ProductDto.builder()
                        .productId(productId)
                        .category("Electronics")
                        .name("Galaxy Phone")
                        .description("Advanced smartphone with cosmic design")
                        .price(999.99f)
                        .build();

        List<ProductDto> products = List.of(product);
        ProductListDto productListDto = ProductListDto.builder().products(products).build();

        when(productService.getProductsByCategory("electronics")).thenReturn(productListDto);

        mockMvc
                .perform(get("/api/v1/products/category/{category}", "electronics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products.length()").value(1))
                .andExpect(jsonPath("$.products[0].category").value("Electronics"))
                .andExpect(jsonPath("$.products[0].name").value("Galaxy Phone"));
    }
}
