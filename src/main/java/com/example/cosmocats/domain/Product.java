package com.example.cosmocats.domain;

import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class Product {
  UUID productId;
  String category;
  String name;
  String description;
  Float price;
}
