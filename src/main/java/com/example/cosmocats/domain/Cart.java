package com.example.cosmocats.domain;

import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.UUID;

@Value
@Builder(toBuilder = true)
public class Cart {
  UUID cartId;
  Long customerId;
  List<Product> products;
  Float totalPrice;
}
