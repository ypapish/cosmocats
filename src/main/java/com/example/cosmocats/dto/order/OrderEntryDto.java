package com.example.cosmocats.dto.order;

import lombok.Builder;
import lombok.Value;

import com.example.cosmocats.dto.product.ProductDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;


@Value
@Builder

public class OrderEntryDto {
    @NotBlank(message = "Product type is required")
    ProductDto product;
    
    @PositiveOrZero(message = "Order amount must be greater than 0")
    Integer amount;
}