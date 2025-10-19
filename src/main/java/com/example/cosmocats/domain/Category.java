package com.example.cosmocats.domain;

import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class Category {
  UUID categoryId;
  String name;
}
