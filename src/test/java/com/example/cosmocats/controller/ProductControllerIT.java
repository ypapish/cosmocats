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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@DisplayName("Product Controller IT")
@Tag("product-service")
class ProductControllerIT extends AbstractIt {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should get product by ID successfully")
    void shouldGetProductById() throws Exception {
        UUID productId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");

        mockMvc
                .perform(get("/api/v1/products/{id}", productId).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(productId.toString()))
                .andExpect(jsonPath("$.name").value("Quantum Phone X1"))
                .andExpect(jsonPath("$.category").value("Electronics"));
    }

    @Test
    @DisplayName("Should get products by category successfully")
    void shouldGetProductsByCategory() throws Exception {
        mockMvc
                .perform(
                        get("/api/v1/products/category/{category}", "Electronics")
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").isArray())
                .andExpect(jsonPath("$.products[0].category").value("Electronics"));
    }

    @Test
    @DisplayName("Should return all products successfully")
    void shouldGetAllProducts() throws Exception {
        mockMvc
                .perform(get("/api/v1/products").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").isArray())
                .andExpect(jsonPath("$.products.length()").value(4));
    }

    @Test
    @DisplayName("Should demonstrate WireMock integration")
    void shouldDemonstrateWireMockIntegration() throws Exception {
        UUID productId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");

        mockMvc
                .perform(get("/api/v1/products/{id}", productId).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(productId.toString()))
                .andExpect(jsonPath("$.name").value("Quantum Phone X1"));
    }
}
