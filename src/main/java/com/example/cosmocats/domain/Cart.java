package com.example.cosmocats.domain;

import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class Cart {
  UUID cartId;
  Long customerId;
  List<Product> products;
  Float totalPrice;
}
