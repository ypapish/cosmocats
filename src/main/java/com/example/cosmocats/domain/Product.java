package com.example.cosmocats.domain;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder(toBuilder = true)
public class Product {
  UUID productId;
  String category;
  String name;
  String description;
  Float price;
}
