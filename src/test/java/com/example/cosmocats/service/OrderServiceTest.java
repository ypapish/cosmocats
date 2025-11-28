package com.example.cosmocats.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.cosmocats.dto.order.OrderDto;
import com.example.cosmocats.dto.order.OrderEntryDto;
import com.example.cosmocats.dto.product.ProductDto;
import com.example.cosmocats.entity.OrderEntity;
import com.example.cosmocats.entity.OrderEntryEntity;
import com.example.cosmocats.entity.ProductEntity;
import com.example.cosmocats.exception.OrderNotFoundException;
import com.example.cosmocats.repository.OrderRepository;
import com.example.cosmocats.repository.ProductRepository;
import com.example.cosmocats.service.mapper.OrderMapper;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

  @Mock
  private OrderRepository orderRepository;

  @Mock
  private ProductRepository productRepository;

  @Mock
  private OrderMapper orderMapper;

  @InjectMocks
  private OrderService orderService;

  private UUID orderId;
  private UUID productId1;
  private UUID productId2;
  private OrderEntity testOrderEntity;
  private OrderDto testOrderDto;
  private ProductEntity testProduct1;
  private ProductEntity testProduct2;

  @BeforeEach
  void setUp() {
    orderId = UUID.randomUUID();
    productId1 = UUID.randomUUID();
    productId2 = UUID.randomUUID();

    testProduct1 =
        ProductEntity.builder()
            .productId(1L)
            .productUuid(productId1)
            .name("Quantum Star Phone")
            .price(BigDecimal.valueOf(999.99))
            .build();

    testProduct2 =
        ProductEntity.builder()
            .productId(2L)
            .productUuid(productId2)
            .name("Galaxy Tablet Pro")
            .price(BigDecimal.valueOf(599.99))
            .build();

    OrderEntryEntity orderEntry1 =
        OrderEntryEntity.builder().product(testProduct1).amount(2).build();

    OrderEntryEntity orderEntry2 =
        OrderEntryEntity.builder().product(testProduct2).amount(1).build();

    testOrderEntity =
        OrderEntity.builder()
            .id(1L)
            .orderUuid(orderId)
            .totalPrice(BigDecimal.valueOf(2599.97))
            .entries(List.of(orderEntry1, orderEntry2))
            .build();

    ProductDto productDto1 =
        ProductDto.builder()
            .productId(productId1)
            .name("Quantum Star Phone")
            .price(999.99f)
            .build();

    ProductDto productDto2 =
        ProductDto.builder().productId(productId2).name("Galaxy Tablet Pro").price(599.99f).build();

    OrderEntryDto orderEntryDto1 = OrderEntryDto.builder().product(productDto1).amount(2).build();

    OrderEntryDto orderEntryDto2 = OrderEntryDto.builder().product(productDto2).amount(1).build();

    testOrderDto =
        OrderDto.builder()
            .id(orderId)
            .entries(List.of(orderEntryDto1, orderEntryDto2))
            .totalPrice(2599.97)
            .build();
  }

  @Test
  void createOrder_WithValidData_ShouldCreateAndReturnOrder() {
    OrderDto newOrderDto =
        OrderDto.builder().entries(testOrderDto.getEntries()).totalPrice(2599.97).build();

    when(productRepository.findByNaturalId(productId1)).thenReturn(Optional.of(testProduct1));
    when(productRepository.findByNaturalId(productId2)).thenReturn(Optional.of(testProduct2));
    when(orderRepository.save(any(OrderEntity.class))).thenReturn(testOrderEntity);
    when(orderMapper.toOrderDto(testOrderEntity)).thenReturn(testOrderDto);

    OrderDto result = orderService.createOrder(newOrderDto);

    assertNotNull(result);
    assertEquals(orderId, result.getId());
    assertEquals(2599.97, result.getTotalPrice());
    assertEquals(2, result.getEntries().size());

    verify(productRepository).findByNaturalId(productId1);
    verify(productRepository).findByNaturalId(productId2);
    verify(orderRepository).save(any(OrderEntity.class));
  }

  @Test
  void createOrder_WithNonExistingProduct_ShouldThrowException() {
    OrderEntryDto invalidEntry =
        OrderEntryDto.builder()
            .product(ProductDto.builder().productId(UUID.randomUUID()).build())
            .amount(1)
            .build();

    OrderDto invalidOrderDto =
        OrderDto.builder().entries(List.of(invalidEntry)).totalPrice(100.0).build();

    when(productRepository.findByNaturalId(any(UUID.class))).thenReturn(Optional.empty());

    assertThrows(RuntimeException.class, () -> orderService.createOrder(invalidOrderDto));

    verify(orderRepository, never()).save(any());
  }

  @Test
  void createOrder_WithEmptyEntries_ShouldCreateOrderWithZeroTotal() {
    OrderDto emptyOrderDto = OrderDto.builder().entries(List.of()).totalPrice(0.0).build();

    OrderEntity emptyOrderEntity =
        OrderEntity.builder()
            .id(1L)
            .orderUuid(orderId)
            .totalPrice(BigDecimal.ZERO)
            .entries(List.of())
            .build();

    OrderDto emptyOrderResultDto =
        OrderDto.builder().id(orderId).entries(List.of()).totalPrice(0.0).build();

    when(orderRepository.save(any(OrderEntity.class))).thenReturn(emptyOrderEntity);
    when(orderMapper.toOrderDto(emptyOrderEntity)).thenReturn(emptyOrderResultDto);

    OrderDto result = orderService.createOrder(emptyOrderDto);

    assertNotNull(result);
    assertEquals(0.0, result.getTotalPrice());
    assertTrue(result.getEntries().isEmpty());

    verify(orderRepository).save(any(OrderEntity.class));
  }

  @Test
  void getOrderById_WithExistingId_ShouldReturnOrder() {
    when(orderRepository.findByNaturalId(orderId)).thenReturn(Optional.of(testOrderEntity));
    when(orderMapper.toOrderDto(testOrderEntity)).thenReturn(testOrderDto);

    OrderDto result = orderService.getOrderById(orderId);

    assertNotNull(result);
    assertEquals(orderId, result.getId());
    assertEquals(2599.97, result.getTotalPrice());

    verify(orderRepository).findByNaturalId(orderId);
  }

  @Test
  void getOrderById_WithNonExistingId_ShouldThrowOrderNotFoundException() {
    UUID nonExistingId = UUID.randomUUID();
    when(orderRepository.findByNaturalId(nonExistingId)).thenReturn(Optional.empty());

    assertThrows(OrderNotFoundException.class, () -> orderService.getOrderById(nonExistingId));

    verify(orderRepository).findByNaturalId(nonExistingId);
  }

  @Test
  void getAllOrders_WithOrders_ShouldReturnOrderList() {
    List<OrderEntity> orderEntities = List.of(testOrderEntity);
    List<OrderDto> orderDtos = List.of(testOrderDto);

    when(orderRepository.findAll()).thenReturn(orderEntities);
    when(orderMapper.toOrderDtoList(orderEntities)).thenReturn(orderDtos);

    List<OrderDto> result = orderService.getAllOrders();

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(orderId, result.get(0).getId());

    verify(orderRepository).findAll();
  }

  @Test
  void getAllOrders_WhenNoOrders_ShouldReturnEmptyList() {
    List<OrderEntity> emptyList = List.of();
    List<OrderDto> emptyDtoList = List.of();

    when(orderRepository.findAll()).thenReturn(emptyList);
    when(orderMapper.toOrderDtoList(emptyList)).thenReturn(emptyDtoList);

    List<OrderDto> result = orderService.getAllOrders();

    assertNotNull(result);
    assertTrue(result.isEmpty());

    verify(orderRepository).findAll();
  }

  @Test
  void updateOrder_WithValidData_ShouldUpdateAndReturnOrder() {
    OrderEntryDto updatedEntry =
        OrderEntryDto.builder()
            .product(ProductDto.builder().productId(productId1).build())
            .amount(3)
            .build();

    OrderDto updateDto =
        OrderDto.builder().entries(List.of(updatedEntry)).totalPrice(2999.97).build();

    OrderEntity updatedOrderEntity =
        OrderEntity.builder()
            .id(1L)
            .orderUuid(orderId)
            .totalPrice(BigDecimal.valueOf(2999.97))
            .entries(List.of(OrderEntryEntity.builder().product(testProduct1).amount(3).build()))
            .build();

    OrderDto updatedOrderDto =
        OrderDto.builder().id(orderId).entries(List.of(updatedEntry)).totalPrice(2999.97).build();

    when(orderRepository.findByNaturalId(orderId)).thenReturn(Optional.of(testOrderEntity));
    when(productRepository.findByNaturalId(productId1)).thenReturn(Optional.of(testProduct1));
    when(orderRepository.save(any(OrderEntity.class))).thenReturn(updatedOrderEntity);
    when(orderMapper.toOrderDto(updatedOrderEntity)).thenReturn(updatedOrderDto);

    OrderDto result = orderService.updateOrder(orderId, updateDto);

    assertNotNull(result);
    assertEquals(2999.97, result.getTotalPrice());
    assertEquals(1, result.getEntries().size());

    verify(orderRepository).findByNaturalId(orderId);
    verify(productRepository).findByNaturalId(productId1);
    verify(orderRepository).save(any(OrderEntity.class));
  }

  @Test
  void updateOrder_WithNonExistingId_ShouldThrowOrderNotFoundException() {
    UUID nonExistingId = UUID.randomUUID();
    when(orderRepository.findByNaturalId(nonExistingId)).thenReturn(Optional.empty());

    assertThrows(
        OrderNotFoundException.class, () -> orderService.updateOrder(nonExistingId, testOrderDto));

    verify(orderRepository).findByNaturalId(nonExistingId);
    verify(orderRepository, never()).save(any());
  }

  @Test
  void updateOrder_WithNonExistingProduct_ShouldThrowException() {
    UUID nonExistingProductId = UUID.randomUUID();
    OrderEntryDto invalidEntry =
        OrderEntryDto.builder()
            .product(ProductDto.builder().productId(nonExistingProductId).build())
            .amount(1)
            .build();

    OrderDto updateDto =
        OrderDto.builder().entries(List.of(invalidEntry)).totalPrice(100.0).build();

    when(orderRepository.findByNaturalId(orderId)).thenReturn(Optional.of(testOrderEntity));
    when(productRepository.findByNaturalId(nonExistingProductId)).thenReturn(Optional.empty());

    assertThrows(RuntimeException.class, () -> orderService.updateOrder(orderId, updateDto));

    verify(orderRepository, never()).save(any());
  }

  @Test
  void deleteOrder_WithExistingId_ShouldDeleteOrder() {
    when(orderRepository.findByNaturalId(orderId)).thenReturn(Optional.of(testOrderEntity));
    doNothing().when(orderRepository).delete(testOrderEntity);

    orderService.deleteOrder(orderId);

    verify(orderRepository).findByNaturalId(orderId);
    verify(orderRepository).delete(testOrderEntity);
  }

  @Test
  void deleteOrder_WithNonExistingId_ShouldThrowOrderNotFoundException() {
    UUID nonExistingId = UUID.randomUUID();
    when(orderRepository.findByNaturalId(nonExistingId)).thenReturn(Optional.empty());

    assertThrows(OrderNotFoundException.class, () -> orderService.deleteOrder(nonExistingId));

    verify(orderRepository).findByNaturalId(nonExistingId);
    verify(orderRepository, never()).delete(any());
  }

  @Test
  void calculateOrderTotal_WithExistingOrder_ShouldReturnTotal() {
    when(orderRepository.findByNaturalId(orderId)).thenReturn(Optional.of(testOrderEntity));

    BigDecimal result = orderService.calculateOrderTotal(orderId);

    assertNotNull(result);
    assertEquals(0, BigDecimal.valueOf(2599.97).compareTo(result));

    verify(orderRepository).findByNaturalId(orderId);
  }

  @Test
  void calculateOrderTotal_WithNonExistingOrder_ShouldThrowOrderNotFoundException() {
    UUID nonExistingId = UUID.randomUUID();
    when(orderRepository.findByNaturalId(nonExistingId)).thenReturn(Optional.empty());

    assertThrows(
        OrderNotFoundException.class, () -> orderService.calculateOrderTotal(nonExistingId));

    verify(orderRepository).findByNaturalId(nonExistingId);
  }

  @Test
  void getOrdersWithTotalGreaterThan_ShouldReturnFilteredOrders() {
    OrderEntity smallOrder =
        OrderEntity.builder()
            .orderUuid(UUID.randomUUID())
            .totalPrice(BigDecimal.valueOf(500.0))
            .entries(List.of())
            .build();

    OrderEntity largeOrder =
        OrderEntity.builder()
            .orderUuid(UUID.randomUUID())
            .totalPrice(BigDecimal.valueOf(1500.0))
            .entries(List.of())
            .build();

    OrderDto largeOrderDto =
        OrderDto.builder()
            .id(largeOrder.getOrderUuid())
            .totalPrice(1500.0)
            .entries(List.of())
            .build();

    List<OrderEntity> allOrders = List.of(smallOrder, largeOrder, testOrderEntity);

    when(orderRepository.findAll()).thenReturn(allOrders);
    when(orderMapper.toOrderDto(largeOrder)).thenReturn(largeOrderDto);
    when(orderMapper.toOrderDto(testOrderEntity)).thenReturn(testOrderDto);

    List<OrderDto> result = orderService.getOrdersWithTotalGreaterThan(BigDecimal.valueOf(1000.0));

    assertNotNull(result);
    assertEquals(2, result.size());
    assertTrue(result.stream().allMatch(order -> order.getTotalPrice() > 1000.0));

    verify(orderRepository).findAll();
  }

  @Test
  void getOrdersWithTotalGreaterThan_WithNoMatches_ShouldReturnEmptyList() {
    OrderEntity smallOrder =
        OrderEntity.builder()
            .orderUuid(UUID.randomUUID())
            .totalPrice(BigDecimal.valueOf(500.0))
            .entries(List.of())
            .build();

    List<OrderEntity> allOrders = List.of(smallOrder);

    when(orderRepository.findAll()).thenReturn(allOrders);

    List<OrderDto> result = orderService.getOrdersWithTotalGreaterThan(BigDecimal.valueOf(1000.0));

    assertNotNull(result);
    assertTrue(result.isEmpty());

    verify(orderRepository).findAll();
  }

  @Test
  void countOrders_ShouldReturnCount() {
    when(orderRepository.count()).thenReturn(5L);

    long result = orderService.countOrders();

    assertEquals(5L, result);
    verify(orderRepository).count();
  }

  @Test
  void calculateTotalPrice_WithMultipleEntries_ShouldReturnCorrectTotal() {
    OrderEntryEntity entry1 =
        OrderEntryEntity.builder()
            .product(ProductEntity.builder().price(BigDecimal.valueOf(100.0)).build())
            .amount(2)
            .build();

    OrderEntryEntity entry2 =
        OrderEntryEntity.builder()
            .product(ProductEntity.builder().price(BigDecimal.valueOf(50.0)).build())
            .amount(3)
            .build();

    OrderEntity order = OrderEntity.builder().entries(List.of(entry1, entry2)).build();

    BigDecimal result = orderService.calculateOrderTotal(orderId);

    when(orderRepository.findByNaturalId(orderId)).thenReturn(Optional.of(order));
    BigDecimal calculatedTotal = orderService.calculateOrderTotal(orderId);

    assertEquals(0, BigDecimal.valueOf(350.0).compareTo(calculatedTotal));
  }
}
