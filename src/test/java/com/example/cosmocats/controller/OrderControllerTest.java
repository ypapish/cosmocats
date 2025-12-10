package com.example.cosmocats.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.cosmocats.dto.order.OrderDto;
import com.example.cosmocats.dto.order.OrderEntryDto;
import com.example.cosmocats.dto.product.ProductDto;
import com.example.cosmocats.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
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

@WebMvcTest(OrderController.class)
class OrderControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private OrderService orderService;

  @Autowired
  private ObjectMapper objectMapper;

  private OrderDto testOrderDto;
  private UUID orderId;
  private UUID productId;

  @BeforeEach
  void setUp() {
    orderId = UUID.randomUUID();
    productId = UUID.randomUUID();

    ProductDto productDto =
        ProductDto.builder()
            .productId(productId)
            .category("Electronics")
            .name("Quantum Star Phone")
            .description("Advanced smartphone")
            .price(999.99f)
            .build();

    OrderEntryDto orderEntryDto = OrderEntryDto.builder().product(productDto).amount(2).build();

    testOrderDto =
        OrderDto.builder().id(orderId).entries(List.of(orderEntryDto)).totalPrice(1999.98).build();
  }

  @Test
  @SneakyThrows
  @WithMockUser(roles = {"USER"})
  void getOrderById_WithValidId_ShouldReturnOrder() {
    Mockito.when(orderService.getOrderById(orderId)).thenReturn(testOrderDto);

    mockMvc
        .perform(get("/api/v1/orders/{id}", orderId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(orderId.toString()))
        .andExpect(jsonPath("$.totalPrice").value(1999.98))
        .andExpect(jsonPath("$.entries.length()").value(1));
  }

  @Test
  @SneakyThrows
  @WithMockUser(roles = {"USER"})
  void getOrderById_WithNonExistingId_ShouldReturnNotFound() {
    UUID nonExistingId = UUID.randomUUID();
    Mockito.when(orderService.getOrderById(nonExistingId))
        .thenThrow(new RuntimeException("Order not found"));

    mockMvc.perform(get("/api/v1/orders/{id}", nonExistingId)).andExpect(status().isNotFound());
  }

  @Test
  @SneakyThrows
  @WithMockUser(roles = {"ADMIN"})
  void getAllOrders_ShouldReturnOrderList() {
    Mockito.when(orderService.getAllOrders()).thenReturn(List.of(testOrderDto));

    mockMvc
        .perform(get("/api/v1/orders"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].id").value(orderId.toString()))
        .andExpect(jsonPath("$[0].totalPrice").value(1999.98));
  }

  @Test
  @SneakyThrows
  @WithMockUser(roles = {"ADMIN"})
  void getAllOrders_WhenNoOrders_ShouldReturnEmptyList() {
    Mockito.when(orderService.getAllOrders()).thenReturn(List.of());

    mockMvc
        .perform(get("/api/v1/orders"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));
  }

  @Test
  @SneakyThrows
  @WithMockUser(roles = {"USER"})
  void createOrder_WithValidData_ShouldReturnCreatedOrder() {
    Mockito.when(orderService.createOrder(Mockito.any(OrderDto.class))).thenReturn(testOrderDto);

    mockMvc
        .perform(
            post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testOrderDto)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(orderId.toString()))
        .andExpect(jsonPath("$.totalPrice").value(1999.98))
        .andExpect(jsonPath("$.entries.length()").value(1));
  }

  @Test
  @SneakyThrows
  @WithMockUser(roles = {"USER"})
  void createOrder_WithInvalidData_ShouldReturnBadRequest() {
    OrderDto invalidOrderDto = OrderDto.builder().id(orderId).totalPrice(-100.0).build();

    mockMvc
        .perform(
            post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidOrderDto)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @SneakyThrows
  @WithMockUser(roles = {"ADMIN"})
  void updateOrder_WithValidData_ShouldReturnUpdatedOrder() {
    OrderDto updatedOrderDto =
        OrderDto.builder()
            .id(orderId)
            .entries(testOrderDto.getEntries())
            .totalPrice(2999.97)
            .build();

    Mockito.when(orderService.updateOrder(Mockito.eq(orderId), Mockito.any(OrderDto.class)))
        .thenReturn(updatedOrderDto);

    mockMvc
        .perform(
            put("/api/v1/orders/{id}", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedOrderDto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(orderId.toString()))
        .andExpect(jsonPath("$.totalPrice").value(2999.97));
  }

  @Test
  @SneakyThrows
  @WithMockUser(roles = {"ADMIN"})
  void updateOrder_WithNonExistingId_ShouldReturnNotFound() {
    UUID nonExistingId = UUID.randomUUID();
    Mockito.when(orderService.updateOrder(Mockito.eq(nonExistingId), Mockito.any(OrderDto.class)))
        .thenThrow(new RuntimeException("Order not found"));

    mockMvc
        .perform(
            put("/api/v1/orders/{id}", nonExistingId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testOrderDto)))
        .andExpect(status().isNotFound());
  }

  @Test
  @SneakyThrows
  @WithMockUser(roles = {"ADMIN"})
  void deleteOrder_WithValidId_ShouldReturnNoContent() {
    Mockito.doNothing().when(orderService).deleteOrder(orderId);

    mockMvc.perform(delete("/api/v1/orders/{id}", orderId)).andExpect(status().isNoContent());

    Mockito.verify(orderService).deleteOrder(orderId);
  }

  @Test
  @SneakyThrows
  @WithMockUser(roles = {"ADMIN"})
  void deleteOrder_WithNonExistingId_ShouldReturnNotFound() {
    UUID nonExistingId = UUID.randomUUID();
    Mockito.doThrow(new RuntimeException("Order not found"))
        .when(orderService)
        .deleteOrder(nonExistingId);

    mockMvc.perform(delete("/api/v1/orders/{id}", nonExistingId)).andExpect(status().isNotFound());

    Mockito.verify(orderService).deleteOrder(nonExistingId);
  }

  @Test
  @SneakyThrows
  @WithMockUser(roles = {"USER"})
  void createOrder_WithInvalidOrderEntry_ShouldReturnBadRequest() {
    ProductDto productDto =
        ProductDto.builder()
            .productId(productId)
            .category("Electronics")
            .name("Quantum Star Phone")
            .description("Advanced smartphone")
            .price(999.99f)
            .build();

    OrderEntryDto invalidEntry = OrderEntryDto.builder().product(productDto).amount(0).build();

    OrderDto invalidOrderDto =
        OrderDto.builder().id(orderId).entries(List.of(invalidEntry)).totalPrice(0.0).build();

    mockMvc
        .perform(
            post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidOrderDto)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @SneakyThrows
  @WithMockUser(roles = {"USER"})
  void createOrder_WithMissingRequiredFields_ShouldReturnBadRequest() {
    OrderDto invalidOrderDto = OrderDto.builder().id(null).totalPrice(null).build();

    mockMvc
        .perform(
            post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidOrderDto)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @SneakyThrows
  void getAllOrders_WithoutAuthentication_ShouldReturnUnauthorized() {
    mockMvc.perform(get("/api/v1/orders")).andExpect(status().isUnauthorized());
  }

  @Test
  @SneakyThrows
  @WithMockUser(roles = {"USER"})
  void getAllOrders_WithUserRole_ShouldReturnForbidden() {
    mockMvc.perform(get("/api/v1/orders")).andExpect(status().isForbidden());
  }

  @Test
  @SneakyThrows
  @WithMockUser(roles = {"ADMIN"})
  void getOrderById_WithAdminRole_ShouldReturnOrder() {
    Mockito.when(orderService.getOrderById(orderId)).thenReturn(testOrderDto);

    mockMvc.perform(get("/api/v1/orders/{id}", orderId)).andExpect(status().isOk());
  }
}
