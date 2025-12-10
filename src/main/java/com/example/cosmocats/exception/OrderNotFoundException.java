package com.example.cosmocats.exception;

import java.util.UUID;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(UUID orderId) {
        super("Order not found with UUID: " + orderId);
    }

    public OrderNotFoundException(Long orderId) {
        super("Order not found with ID: " + orderId);
    }
}