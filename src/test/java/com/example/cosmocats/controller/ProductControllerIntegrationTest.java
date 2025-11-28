package com.example.cosmocats.controller;

import com.example.cosmocats.AbstractIt;
import com.example.cosmocats.entity.CategoryEntity;
import com.example.cosmocats.entity.ProductEntity;
import com.example.cosmocats.repository.CategoryRepository;
import com.example.cosmocats.repository.ProductRepository;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class ProductControllerIntegrationTest extends AbstractIt {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private CategoryEntity electronicsCategory;
    private CategoryEntity booksCategory;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        
        electronicsCategory = CategoryEntity.builder()
                .categoryUuid(UUID.randomUUID())
                .name("Electronics")
                .build();
        
        booksCategory = CategoryEntity.builder()
                .categoryUuid(UUID.randomUUID())
                .name("Books")
                .build();
        
        categoryRepository.save(electronicsCategory);
        categoryRepository.save(booksCategory);
    }

    @Test
    @SneakyThrows
    void getAllProducts_WithProducts_ShouldReturnAllProducts() {
        ProductEntity product1 = ProductEntity.builder()
                .productUuid(UUID.randomUUID())
                .category(electronicsCategory)
                .name("Quantum Star Phone")
                .description("Advanced smartphone")
                .price(BigDecimal.valueOf(999.99))
                .build();
        
        ProductEntity product2 = ProductEntity.builder()
                .productUuid(UUID.randomUUID())
                .category(booksCategory)
                .name("Cosmic Travel Guide")
                .description("Space exploration guide")
                .price(BigDecimal.valueOf(29.99))
                .build();
        
        productRepository.save(product1);
        productRepository.save(product2);

        mockMvc.perform(get("/api/v1/products")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products.length()").value(2))
                .andExpect(jsonPath("$.products[0].name").exists())
                .andExpect(jsonPath("$.products[1].name").exists());
    }

    @Test
    @SneakyThrows
    void getAllProducts_WhenNoProducts_ShouldReturnEmptyList() {
        mockMvc.perform(get("/api/v1/products")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products.length()").value(0));
    }

    @Test
    @SneakyThrows
    void getProductById_WithExistingId_ShouldReturnProduct() {
        ProductEntity product = ProductEntity.builder()
                .productUuid(UUID.randomUUID())
                .category(electronicsCategory)
                .name("Galaxy Tablet Pro")
                .description("Professional tablet")
                .price(BigDecimal.valueOf(599.99))
                .build();
        ProductEntity savedProduct = productRepository.save(product);

        mockMvc.perform(get("/api/v1/products/{id}", savedProduct.getProductUuid())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Galaxy Tablet Pro"))
                .andExpect(jsonPath("$.category").value("Electronics"))
                .andExpect(jsonPath("$.price").value(599.99))
                .andExpect(jsonPath("$.productId").value(savedProduct.getProductUuid().toString()));
    }

    @Test
    @SneakyThrows
    void getProductById_WithNonExistingId_ShouldReturnNotFound() {
        UUID nonExistingId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/products/{id}", nonExistingId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Product Not Found"));
    }

    @Test
    @SneakyThrows
    void getProductsByCategory_WithExistingCategory_ShouldReturnProducts() {
        ProductEntity product1 = ProductEntity.builder()
                .productUuid(UUID.randomUUID())
                .category(electronicsCategory)
                .name("Star Phone X1")
                .description("Smartphone")
                .price(BigDecimal.valueOf(899.99))
                .build();
        
        ProductEntity product2 = ProductEntity.builder()
                .productUuid(UUID.randomUUID())
                .category(electronicsCategory)
                .name("Cosmic Laptop")
                .description("Laptop for space calculations")
                .price(BigDecimal.valueOf(1299.99))
                .build();
        
        ProductEntity bookProduct = ProductEntity.builder()
                .productUuid(UUID.randomUUID())
                .category(booksCategory)
                .name("Space History Book")
                .description("History of space exploration")
                .price(BigDecimal.valueOf(19.99))
                .build();
        
        productRepository.save(product1);
        productRepository.save(product2);
        productRepository.save(bookProduct);

        mockMvc.perform(get("/api/v1/products/category/Electronics")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products.length()").value(2))
                .andExpect(jsonPath("$.products[0].category").value("Electronics"))
                .andExpect(jsonPath("$.products[1].category").value("Electronics"));
    }

    @Test
    @SneakyThrows
    void getProductsByCategory_WithNonExistingCategory_ShouldReturnEmptyList() {
        mockMvc.perform(get("/api/v1/products/category/NonExistingCategory")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products.length()").value(0));
    }

    @Test
    @SneakyThrows
    void getProductsByCategory_WithEmptyCategory_ShouldReturnEmptyList() {
        mockMvc.perform(get("/api/v1/products/category/EmptyCategory")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products.length()").value(0));
    }
}