package com.example.cosmocats.controller;

import com.example.cosmocats.AbstractIt;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
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

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should get product with external stock info")
    void shouldGetProductWithStockInfo() throws Exception {
        // Arrange
        UUID productId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
        
        stubFor(WireMock.get("/inventory/v1/stock/" + productId)
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody("""
                    {
                        "productId": "550e8400-e29b-41d4-a716-446655440001",
                        "inStock": true,
                        "quantity": 15,
                        "location": "WAREHOUSE_A"
                    }
                    """)));

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/{id}", productId)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.productId").value(productId.toString()))
            .andExpect(jsonPath("$.name").value("Quantum Phone X1"))
            .andExpect(jsonPath("$.category").value("Electronics"))
            .andExpect(jsonPath("$.description").value("Advanced smartphone with quantum processor"))
            .andExpect(jsonPath("$.price").value(999.99));

        // Verify external service was called
        verify(getRequestedFor(urlEqualTo("/inventory/v1/stock/" + productId)));
    }

    @Test
    @DisplayName("Should get products by category with external validation")
    void shouldGetProductsByCategoryWithExternalValidation() throws Exception {
        // Arrange
        stubFor(WireMock.get("/categories/v1/validate/Electronics")
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody("""
                    {
                        "category": "Electronics",
                        "valid": true,
                        "active": true
                    }
                    """)));

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/category/{category}", "Electronics")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.products").isArray())
            .andExpect(jsonPath("$.products[0].category").value("Electronics"))
            .andExpect(jsonPath("$.products[0].name").exists());

        // Verify external service was called
        verify(getRequestedFor(urlEqualTo("/categories/v1/validate/Electronics")));
    }
}