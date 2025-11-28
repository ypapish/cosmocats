package com.example.cosmocats.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.cosmocats.AbstractIt;
import com.example.cosmocats.dto.order.OrderDto;
import com.example.cosmocats.dto.order.OrderEntryDto;
import com.example.cosmocats.dto.product.ProductDto;
import com.example.cosmocats.entity.CategoryEntity;
import com.example.cosmocats.entity.OrderEntity;
import com.example.cosmocats.entity.ProductEntity;
import com.example.cosmocats.repository.CategoryRepository;
import com.example.cosmocats.repository.OrderRepository;
import com.example.cosmocats.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
class OrderControllerIntegrationTest extends AbstractIt {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private OrderRepository orderRepository;

  @Autowired
  private ProductRepository productRepository;

  @Autowired
  private CategoryRepository categoryRepository;

  @Autowired
  private ObjectMapper objectMapper;

  private CategoryEntity testCategory;
  private ProductEntity testProduct1;
  private ProductEntity testProduct2;

  @BeforeEach
  void setUp() {
    orderRepository.deleteAll();
    productRepository.deleteAll();
    categoryRepository.deleteAll();

    testCategory =
        CategoryEntity.builder().categoryUuid(UUID.randomUUID()).name("Electronics").build();
    categoryRepository.save(testCategory);

    testProduct1 =
        ProductEntity.builder()
            .productUuid(UUID.randomUUID())
            .category(testCategory)
            .name("Quantum Star Phone")
            .description("Advanced smartphone")
            .price(BigDecimal.valueOf(999.99))
            .build();

    testProduct2 =
        ProductEntity.builder()
            .productUuid(UUID.randomUUID())
            .category(testCategory)
            .name("Galaxy Tablet Pro")
            .description("Professional tablet")
            .price(BigDecimal.valueOf(599.99))
            .build();

    productRepository.save(testProduct1);
    productRepository.save(testProduct2);
  }

  @Test
  @SneakyThrows
  void createOrder_WithValidData_ShouldCreateOrder() {
    ProductDto productDto1 =
        ProductDto.builder()
            .productId(testProduct1.getProductUuid())
            .category("Electronics")
            .name("Quantum Star Phone")
            .description("Advanced smartphone")
            .price(999.99f)
            .build();

    OrderEntryDto entry1 = OrderEntryDto.builder().product(productDto1).amount(2).build();

    OrderDto orderDto = OrderDto.builder().entries(List.of(entry1)).totalPrice(1999.98).build();

    mockMvc
        .perform(
            post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderDto)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.totalPrice").value(1999.98))
        .andExpect(jsonPath("$.entries.length()").value(1))
        .andExpect(jsonPath("$.id").exists());

    assertEquals(1, orderRepository.count());
  }

  @Test
  @SneakyThrows
  void createOrder_WithMultipleProducts_ShouldCreateOrder() {
    ProductDto productDto1 =
        ProductDto.builder()
            .productId(testProduct1.getProductUuid())
            .category("Electronics")
            .name("Quantum Star Phone")
            .description("Advanced smartphone")
            .price(999.99f)
            .build();

    ProductDto productDto2 =
        ProductDto.builder()
            .productId(testProduct2.getProductUuid())
            .category("Electronics")
            .name("Galaxy Tablet Pro")
            .description("Professional tablet")
            .price(599.99f)
            .build();

    OrderEntryDto entry1 = OrderEntryDto.builder().product(productDto1).amount(1).build();

    OrderEntryDto entry2 = OrderEntryDto.builder().product(productDto2).amount(3).build();

    OrderDto orderDto =
        OrderDto.builder().entries(List.of(entry1, entry2)).totalPrice(2799.96).build();

    mockMvc
        .perform(
            post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderDto)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.totalPrice").value(2799.96))
        .andExpect(jsonPath("$.entries.length()").value(2))
        .andExpect(jsonPath("$.id").exists());

    assertEquals(1, orderRepository.count());
  }

  @Test
  @SneakyThrows
  void createOrder_WithNonExistingProduct_ShouldReturnBadRequest() {
    UUID nonExistingProductId = UUID.randomUUID();

    ProductDto invalidProductDto =
        ProductDto.builder()
            .productId(nonExistingProductId)
            .category("Electronics")
            .name("Non Existing Product")
            .description("This product doesn't exist")
            .price(100.0f)
            .build();

    OrderEntryDto entry = OrderEntryDto.builder().product(invalidProductDto).amount(1).build();

    OrderDto orderDto = OrderDto.builder().entries(List.of(entry)).totalPrice(100.0).build();

    mockMvc
        .perform(
            post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderDto)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @SneakyThrows
  void getOrderById_WithExistingId_ShouldReturnOrder() {
    OrderEntity order =
        OrderEntity.builder()
            .orderUuid(UUID.randomUUID())
            .totalPrice(BigDecimal.valueOf(999.99))
            .build();
    OrderEntity savedOrder = orderRepository.save(order);

    mockMvc
        .perform(
            get("/api/v1/orders/{id}", savedOrder.getOrderUuid())
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(savedOrder.getOrderUuid().toString()))
        .andExpect(jsonPath("$.totalPrice").value(999.99));
  }

  @Test
  @SneakyThrows
  void getOrderById_WithNonExistingId_ShouldReturnNotFound() {
    UUID nonExistingId = UUID.randomUUID();

    mockMvc
        .perform(get("/api/v1/orders/{id}", nonExistingId).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.title").value("Order Not Found"));
  }

  @Test
  @SneakyThrows
  void getAllOrders_WithOrders_ShouldReturnAllOrders() {
    OrderEntity order1 =
        OrderEntity.builder()
            .orderUuid(UUID.randomUUID())
            .totalPrice(BigDecimal.valueOf(999.99))
            .build();

    OrderEntity order2 =
        OrderEntity.builder()
            .orderUuid(UUID.randomUUID())
            .totalPrice(BigDecimal.valueOf(599.99))
            .build();

    orderRepository.save(order1);
    orderRepository.save(order2);

    mockMvc
        .perform(get("/api/v1/orders").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].totalPrice").exists())
        .andExpect(jsonPath("$[1].totalPrice").exists());
  }

  @Test
  @SneakyThrows
  void getAllOrders_WhenNoOrders_ShouldReturnEmptyList() {
    mockMvc
        .perform(get("/api/v1/orders").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));
  }

  @Test
  @SneakyThrows
  void updateOrder_WithValidData_ShouldUpdateOrder() {
    OrderEntity existingOrder =
        OrderEntity.builder()
            .orderUuid(UUID.randomUUID())
            .totalPrice(BigDecimal.valueOf(500.0))
            .build();
    OrderEntity savedOrder = orderRepository.save(existingOrder);

    ProductDto productDto =
        ProductDto.builder()
            .productId(testProduct1.getProductUuid())
            .category("Electronics")
            .name("Quantum Star Phone")
            .description("Advanced smartphone")
            .price(999.99f)
            .build();

    OrderEntryDto entry = OrderEntryDto.builder().product(productDto).amount(2).build();

    OrderDto updateDto = OrderDto.builder().entries(List.of(entry)).totalPrice(1999.98).build();

    mockMvc
        .perform(
            put("/api/v1/orders/{id}", savedOrder.getOrderUuid())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalPrice").value(1999.98))
        .andExpect(jsonPath("$.entries.length()").value(1));

    OrderEntity updatedOrder = orderRepository.findByNaturalId(savedOrder.getOrderUuid()).get();
    assertEquals(0, BigDecimal.valueOf(1999.98).compareTo(updatedOrder.getTotalPrice()));
  }

  @Test
  @SneakyThrows
  void updateOrder_WithNonExistingId_ShouldReturnNotFound() {
    UUID nonExistingId = UUID.randomUUID();

    OrderDto updateDto = OrderDto.builder().totalPrice(100.0).build();

    mockMvc
        .perform(
            put("/api/v1/orders/{id}", nonExistingId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
        .andExpect(status().isNotFound());
  }

  @Test
  @SneakyThrows
  void deleteOrder_WithExistingId_ShouldDeleteOrder() {
    OrderEntity order =
        OrderEntity.builder()
            .orderUuid(UUID.randomUUID())
            .totalPrice(BigDecimal.valueOf(999.99))
            .build();
    OrderEntity savedOrder = orderRepository.save(order);

    mockMvc
        .perform(delete("/api/v1/orders/{id}", savedOrder.getOrderUuid()))
        .andExpect(status().isNoContent());

    assertFalse(orderRepository.findByNaturalId(savedOrder.getOrderUuid()).isPresent());
  }

  @Test
  @SneakyThrows
  void deleteOrder_WithNonExistingId_ShouldReturnNotFound() {
    UUID nonExistingId = UUID.randomUUID();

    mockMvc.perform(delete("/api/v1/orders/{id}", nonExistingId)).andExpect(status().isNotFound());
  }

  @Test
  @SneakyThrows
  void createOrder_WithZeroAmount_ShouldReturnBadRequest() {
    ProductDto productDto =
        ProductDto.builder()
            .productId(testProduct1.getProductUuid())
            .category("Electronics")
            .name("Quantum Star Phone")
            .description("Advanced smartphone")
            .price(999.99f)
            .build();

    OrderEntryDto entry = OrderEntryDto.builder().product(productDto).amount(0).build();

    OrderDto orderDto = OrderDto.builder().entries(List.of(entry)).totalPrice(0.0).build();

    mockMvc
        .perform(
            post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderDto)))
        .andExpect(status().isBadRequest());
  }
}
