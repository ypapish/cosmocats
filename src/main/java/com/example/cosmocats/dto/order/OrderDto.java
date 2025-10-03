package com.example.cosmocats.dto.order;

import java.util.List;
import java.util.UUID;

import lombok.Builder;
import lombok.Value;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;


@Value
@Builder

public class OrderDto {
    @NotNull(message = "Id is required")
    UUID id;

    List<OrderEntryDto> entries;
    
    @PositiveOrZero(message = "Total price can not be less than 0")
    Double totalPrice;
}