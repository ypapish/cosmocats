package com.example.cosmocats.dto;

import com.example.cosmocats.dto.product.ProductDto;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;


@Value
@Builder

public class CartDto {
    @NotNull(message = "Cart id is required")
    UUID cartId;

    @NotNull(message = "CustomerId is required")
    Long customerId;

    List<ProductDto> products;
    
    @PositiveOrZero(message = "TotalPrice can not be less than 0")
    Float totalPrice;
}
