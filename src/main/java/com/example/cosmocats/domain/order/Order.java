package com.example.cosmocats.domain.order;

import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.UUID;

@Value
@Builder
public class Order {
    UUID id;
    List<OrderEntry> entries;
    Double totalPrice;
}
