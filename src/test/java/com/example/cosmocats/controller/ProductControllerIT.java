package com.example.cosmocats.controller;

import com.example.cosmocats.AbstractIt;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@DisplayName("Product Controller IT")
@Tag("product-service")
class ProductControllerIT extends AbstractIt {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should get product by ID successfully")
    void shouldGetProductById() throws Exception {
        // Arrange
        UUID productId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/{id}", productId)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.productId").value(productId.toString()))
            .andExpect(jsonPath("$.name").value("Quantum Phone X1"))
            .andExpect(jsonPath("$.category").value("Electronics"));
    }

    @Test
    @DisplayName("Should get product by ID with external service integration")
    void shouldGetProductByIdWithExternalServices() throws Exception {
        // Arrange
        UUID productId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
        
        // Stub external services - демонструємо інтеграцію
        stubInventoryCheck(productId, true, 50);
        stubPriceCalculation(productId, 999.99, 899.99);

        // Act & Assert - тестуємо лише базову функціональність
        mockMvc.perform(get("/api/v1/products/{id}", productId)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.productId").value(productId.toString()))
            .andExpect(jsonPath("$.name").value("Quantum Phone X1"))
            .andExpect(jsonPath("$.category").value("Electronics"));

        // Verify that external services were called
        wireMockServer.verify(exactly(1), 
            getRequestedFor(urlEqualTo("/api/inventory/" + productId)));
    }

    @Test
    @DisplayName("Should handle inventory service errors gracefully")
    void shouldHandleInventoryServiceErrors() throws Exception {
        // Arrange
        UUID productId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
        
        stubInventoryCheckError(productId);

        // Act & Assert - продукт повинен повертатися навіть при помилці інвентаризації
        mockMvc.perform(get("/api/v1/products/{id}", productId)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.productId").value(productId.toString()))
            .andExpect(jsonPath("$.name").value("Quantum Phone X1"));

        // Verify that the call was attempted
        wireMockServer.verify(exactly(1), 
            getRequestedFor(urlEqualTo("/api/inventory/" + productId)));
    }

    @Test
    @DisplayName("Should integrate with pricing service")
    void shouldIntegrateWithPricingService() throws Exception {
        // Arrange
        UUID productId = UUID.fromString("550e8400-e29b-41d4-a716-446655440003");
        
        stubPriceCalculation(productId, 4.99, 3.99);

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/{id}", productId)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.productId").value(productId.toString()))
            .andExpect(jsonPath("$.name").value("Astro Nutrition Bar"));

        // Verify pricing service call
        wireMockServer.verify(exactly(1), 
            postRequestedFor(urlEqualTo("/api/pricing/calculate")));
    }

    @Test
    @DisplayName("Should handle pricing service timeout gracefully")
    void shouldHandlePricingServiceTimeout() throws Exception {
        // Arrange
        UUID productId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
        
        // Stub timeout for pricing service
        wireMockServer.stubFor(
            post(urlEqualTo("/api/pricing/calculate"))
                .willReturn(aResponse()
                    .withStatus(408)
                    .withFixedDelay(2000)
                    .withBody("{\"error\":\"Pricing service timeout\"}"))
        );

        // Act & Assert - повинен обробити таймаут і повернути продукт
        mockMvc.perform(get("/api/v1/products/{id}", productId)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.productId").value(productId.toString()))
            .andExpect(jsonPath("$.name").value("Quantum Phone X1"));

        // Verify the call was attempted
        wireMockServer.verify(exactly(1), 
            postRequestedFor(urlEqualTo("/api/pricing/calculate")));
    }

    @Test
    @DisplayName("Should get products by category with external service calls")
    void shouldGetProductsByCategoryWithExternalServices() throws Exception {
        // Arrange
        UUID product1 = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
        UUID product2 = UUID.fromString("550e8400-e29b-41d4-a716-446655440004");
        
        stubInventoryCheck(product1, true, 25);
        stubInventoryCheck(product2, false, 0);

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/category/{category}", "Electronics")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.products").isArray())
            .andExpect(jsonPath("$.products[0].category").value("Electronics"));

        // Verify external service calls
        wireMockServer.verify(exactly(2), 
            getRequestedFor(urlMatching("/api/inventory/.*")));
    }
}