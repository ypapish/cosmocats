package com.example.cosmocats.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class CategoryDto {
  @NotNull(message = "Category id is required")
  UUID categoryId;

  @Size(min = 6, max = 100, message = "Category name must be between 6 and 100 characters")
  String name;
}
