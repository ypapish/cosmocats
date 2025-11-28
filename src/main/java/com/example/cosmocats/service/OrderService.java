package com.example.cosmocats.service;

import com.example.cosmocats.dto.order.OrderDto;
import com.example.cosmocats.dto.order.OrderEntryDto;
import com.example.cosmocats.entity.OrderEntity;
import com.example.cosmocats.entity.OrderEntryEntity;
import com.example.cosmocats.entity.ProductEntity;
import com.example.cosmocats.exception.OrderNotFoundException;
import com.example.cosmocats.repository.OrderRepository;
import com.example.cosmocats.repository.ProductRepository;
import com.example.cosmocats.service.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;

    @Transactional
    public OrderDto createOrder(OrderDto orderDto) {
        log.info("Creating new order");

        OrderEntity order = OrderEntity.builder()
            .orderUuid(UUID.randomUUID())
            .totalPrice(BigDecimal.ZERO)
            .build();

        if (orderDto.getEntries() != null) {
            for (OrderEntryDto entryDto : orderDto.getEntries()) {
                ProductEntity product = productRepository.findByNaturalId(entryDto.getProduct().getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + entryDto.getProduct().getProductId()));

                OrderEntryEntity orderEntry = OrderEntryEntity.builder()
                    .order(order)
                    .product(product)
                    .amount(entryDto.getAmount())
                    .build();

                order.getEntries().add(orderEntry);
            }
        }

        BigDecimal totalPrice = calculateTotalPrice(order);
        order.setTotalPrice(totalPrice);

        OrderEntity savedOrder = orderRepository.save(order);
        log.info("Order created successfully with UUID: {}", savedOrder.getOrderUuid());

        return orderMapper.toOrderDto(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderDto getOrderById(UUID orderUuid) {
        log.info("Fetching order by UUID: {}", orderUuid);

        OrderEntity order = orderRepository.findByNaturalId(orderUuid)
            .orElseThrow(() -> {
                log.warn("Order not found with UUID: {}", orderUuid);
                return new OrderNotFoundException(orderUuid);
            });

        log.info("Order found with total price: {}", order.getTotalPrice());
        return orderMapper.toOrderDto(order);
    }

    @Transactional(readOnly = true)
    public List<OrderDto> getAllOrders() {
        log.info("Fetching all orders");

        List<OrderEntity> orders = orderRepository.findAll();
        log.info("Found {} orders", orders.size());

        return orderMapper.toOrderDtoList(orders);
    }

    @Transactional
    public OrderDto updateOrder(UUID orderUuid, OrderDto updateDto) {
        log.info("Updating order with UUID: {}", orderUuid);

        OrderEntity existingOrder = orderRepository.findByNaturalId(orderUuid)
            .orElseThrow(() -> {
                log.warn("Order not found for update with UUID: {}", orderUuid);
                return new OrderNotFoundException(orderUuid);
            });

        existingOrder.getEntries().clear();

        if (updateDto.getEntries() != null) {
            for (OrderEntryDto entryDto : updateDto.getEntries()) {
                ProductEntity product = productRepository.findByNaturalId(entryDto.getProduct().getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + entryDto.getProduct().getProductId()));

                OrderEntryEntity orderEntry = OrderEntryEntity.builder()
                    .order(existingOrder)
                    .product(product)
                    .amount(entryDto.getAmount())
                    .build();

                existingOrder.getEntries().add(orderEntry);
            }
        }

        BigDecimal totalPrice = calculateTotalPrice(existingOrder);
        existingOrder.setTotalPrice(totalPrice);

        OrderEntity savedOrder = orderRepository.save(existingOrder);
        log.info("Order updated successfully with UUID: {}", savedOrder.getOrderUuid());

        return orderMapper.toOrderDto(savedOrder);
    }

    @Transactional
    public void deleteOrder(UUID orderUuid) {
        log.info("Deleting order with UUID: {}", orderUuid);

        OrderEntity order = orderRepository.findByNaturalId(orderUuid)
            .orElseThrow(() -> {
                log.info("Order not found for deletion with UUID: {}", orderUuid);
                return new OrderNotFoundException(orderUuid);
            });

        orderRepository.delete(order);
        log.info("Order deleted successfully with UUID: {}", orderUuid);
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateOrderTotal(UUID orderUuid) {
        log.info("Calculating total for order: {}", orderUuid);

        OrderEntity order = orderRepository.findByNaturalId(orderUuid)
            .orElseThrow(() -> new OrderNotFoundException(orderUuid));

        return calculateTotalPrice(order);
    }

    private BigDecimal calculateTotalPrice(OrderEntity order) {
        return order.getEntries().stream()
            .map(entry -> entry.getProduct().getPrice().multiply(BigDecimal.valueOf(entry.getAmount())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional(readOnly = true)
    public List<OrderDto> getOrdersWithTotalGreaterThan(BigDecimal minTotal) {
        log.info("Fetching orders with total greater than: {}", minTotal);
        
        return orderRepository.findAll().stream()
            .filter(order -> order.getTotalPrice().compareTo(minTotal) > 0)
            .map(orderMapper::toOrderDto)
            .toList();
    }

    @Transactional(readOnly = true)
    public long countOrders() {
        log.info("Counting all orders");
        return orderRepository.count();
    }
}