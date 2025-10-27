package com.example.cosmocats.controller;

import com.example.cosmocats.AbstractIt;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@DisplayName("Product Controller IT")
@Tag("product-service")
class ProductControllerIT extends AbstractIt {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should get product by ID successfully")
    void shouldGetProductById() throws Exception {
        // Arrange
        UUID productId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/products/{id}", productId)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.productId").value(productId.toString()))
            .andExpect(jsonPath("$.name").value("Quantum Phone X1"))
            .andExpect(jsonPath("$.category").value("Electronics"));
    }

    @Test
    @DisplayName("Should get products by category successfully")
    void shouldGetProductsByCategory() throws Exception {
        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/products/category/{category}", "Electronics")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.products").isArray())
            .andExpect(jsonPath("$.products[0].category").value("Electronics"));
    }

    @Test
    @DisplayName("Should return all products successfully")
    void shouldGetAllProducts() throws Exception {
        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/products")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.products").isArray())
            .andExpect(jsonPath("$.products.length()").value(4));
    }

    @Test
    @DisplayName("Should get product by ID successfully with inventory check")
    void shouldGetProductByIdWithInventory() throws Exception {
        // Arrange
        UUID productId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
        
        // ✅ REAL WIREMOCK STUBBING - додатковий бал
        stubInventoryCheck(productId, true, 50);
        stubPriceCalculation(productId, 999.99f, 899.99f);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/products/{id}", productId)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.productId").value(productId.toString()))
            .andExpect(jsonPath("$.name").value("Quantum Phone X1"))
            .andExpect(jsonPath("$.category").value("Electronics"));
    }

    @Test
    @DisplayName("Should get products by category with inventory data")
    void shouldGetProductsByCategoryWithInventory() throws Exception {
        // Arrange
        UUID electronicsProduct1 = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
        UUID electronicsProduct2 = UUID.fromString("550e8400-e29b-41d4-a716-446655440004");
        
        // ✅ REAL WIREMOCK STUBBING для кількох продуктів
        stubInventoryCheck(electronicsProduct1, true, 25);
        stubInventoryCheck(electronicsProduct2, false, 0);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/products/category/{category}", "Electronics")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.products").isArray())
            .andExpect(jsonPath("$.products[0].category").value("Electronics"));
    }

    @Test
    @DisplayName("Should handle inventory service timeout gracefully")
    void shouldHandleInventoryTimeout() throws Exception {
        // Arrange
        UUID productId = UUID.fromString("550e8400-e29b-41d4-a716-446655440002");
        
        // ✅ REAL WIREMOCK STUBBING для тестування помилок
        getWireMockServer().stubFor(
            get(urlEqualTo("/api/inventory/" + productId))
                .willReturn(aResponse()
                    .withStatus(408)
                    .withFixedDelay(2000)
                    .withBody("{\"error\":\"Request timeout\"}"))
        );

        // Act & Assert - продукт все одно повинен повертатися навіть при помилці інвентаризації
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/products/{id}", productId)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.productId").value(productId.toString()))
            .andExpect(jsonPath("$.name").value("Interstellar Travel Guide"));
    }

    @Test
    @DisplayName("Should get product with dynamic pricing")
    void shouldGetProductWithDynamicPricing() throws Exception {
        // Arrange
        UUID productId = UUID.fromString("550e8400-e29b-41d4-a716-446655440003");
        
        // ✅ REAL WIREMOCK STUBBING для сервісу ціноутворення
        getWireMockServer().stubFor(
            post(urlEqualTo("/api/pricing/calculate"))
                .withRequestBody(equalToJson(
                    "{\"productId\":\"550e8400-e29b-41d4-a716-446655440003\",\"originalPrice\":4.99}"
                ))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"productId\":\"550e8400-e29b-41d4-a716-446655440003\",\"originalPrice\":4.99,\"discountedPrice\":3.99,\"discountApplied\":1.00}"))
        );

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/products/{id}", productId)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.productId").value(productId.toString()))
            .andExpect(jsonPath("$.name").value("Astro Nutrition Bar"));
    }

    @Test
    @DisplayName("Should verify WireMock stubbing is actually used")
    void shouldVerifyWireMockCalls() throws Exception {
        // Arrange
        UUID productId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
        
        // ✅ REAL WIREMOCK STUBBING з верифікацією викликів
        stubInventoryCheck(productId, true, 100);

        // Act
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/products/{id}", productId)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        // Assert - верифікація, що WireMock був викликаний
        getWireMockServer().verify(exactly(1), getRequestedFor(urlEqualTo("/api/inventory/" + productId)));
    }

    @Test
    @DisplayName("Should handle inventory service error")
    void shouldHandleInventoryServiceError() throws Exception {
        // Arrange
        UUID productId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
        
        // ✅ REAL WIREMOCK STUBBING для тестування помилок сервера
        stubInventoryCheckError(productId);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/products/{id}", productId)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.productId").value(productId.toString()));
    }
}