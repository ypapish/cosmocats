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
    @DisplayName("Should get product by ID with inventory data")
    void shouldGetProductByIdWithInventory() throws Exception {
        // Arrange
        UUID productId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
        
        stubInventoryCheck(productId, true, 50);
        stubPriceCalculation(productId, 999.99, 899.99);

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/{id}", productId)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.productId").value(productId.toString()))
            .andExpect(jsonPath("$.name").value("Quantum Phone X1"))
            .andExpect(jsonPath("$.price").value(899.99));
    }

    @Test
    @DisplayName("Should handle out of stock product")
    void shouldHandleOutOfStockProduct() throws Exception {
        // Arrange
        UUID productId = UUID.fromString("550e8400-e29b-41d4-a716-446655440002");
        
        stubInventoryCheck(productId, false, 0);
        stubPriceCalculation(productId, 29.99, 29.99);

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/{id}", productId)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.productId").value(productId.toString()))
            .andExpect(jsonPath("$.name").value("Interstellar Travel Guide"))
            .andExpect(jsonPath("$.inStock").value(false));
    }

    @Test
    @DisplayName("Should handle inventory service unavailable")
    void shouldHandleInventoryServiceUnavailable() throws Exception {
        // Arrange
        UUID productId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
        
        stubInventoryCheckError(productId);
        stubPriceCalculation(productId, 999.99, 899.99);

        // Act & Assert - продукт повинен повертатися навіть при помилці інвентаризації
        mockMvc.perform(get("/api/v1/products/{id}", productId)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.productId").value(productId.toString()))
            .andExpect(jsonPath("$.name").value("Quantum Phone X1"));
    }

    @Test
    @DisplayName("Should apply discount for promotional product")
    void shouldApplyDiscountForPromotionalProduct() throws Exception {
        // Arrange
        UUID productId = UUID.fromString("550e8400-e29b-41d4-a716-446655440003");
        
        stubInventoryCheck(productId, true, 100);
        stubPriceCalculation(productId, 4.99, 3.99);

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/{id}", productId)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.productId").value(productId.toString()))
            .andExpect(jsonPath("$.name").value("Astro Nutrition Bar"))
            .andExpect(jsonPath("$.price").value(3.99));
    }

    @Test
    @DisplayName("Should get products by category with mixed inventory status")
    void shouldGetProductsByCategoryWithMixedInventory() throws Exception {
        // Arrange
        UUID inStockProduct = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
        UUID outOfStockProduct = UUID.fromString("550e8400-e29b-41d4-a716-446655440004");
        
        stubInventoryCheck(inStockProduct, true, 25);
        stubInventoryCheck(outOfStockProduct, false, 0);

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/category/{category}", "Electronics")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.products").isArray())
            .andExpect(jsonPath("$.products[0].category").value("Electronics"))
            .andExpect(jsonPath("$.products[0].inStock").value(true))
            .andExpect(jsonPath("$.products[1].inStock").value(false));
    }

    @Test
    @DisplayName("Should handle pricing service timeout gracefully")
    void shouldHandlePricingServiceTimeout() throws Exception {
        // Arrange
        UUID productId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
        
        stubInventoryCheck(productId, true, 50);
        
        // Stub для таймауту ціноутворення
        wireMockServer.stubFor(
            post(urlEqualTo("/api/pricing/calculate"))
                .willReturn(aResponse()
                    .withStatus(408)
                    .withFixedDelay(2000)
                    .withBody("{\"error\":\"Pricing service timeout\"}"))
        );

        // Act & Assert - повинен використовувати оригінальну ціну при таймауті
        mockMvc.perform(get("/api/v1/products/{id}", productId)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.productId").value(productId.toString()))
            .andExpect(jsonPath("$.name").value("Quantum Phone X1"))
            .andExpect(jsonPath("$.price").value(999.99)); // оригінальна ціна
    }
}