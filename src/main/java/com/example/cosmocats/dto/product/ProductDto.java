package com.example.cosmocats.dto.product;

import com.example.cosmocats.validation.CosmicWordCheck;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ProductDto {
  @NotNull(message = "ProductId is required")
  UUID productId;

  @NotBlank(message = "Category is required")
  @Size(max = 100, message = "Category must be at most 100 characters")
  String category;

  @NotBlank(message = "Product name is required")
  @Size(max = 100, message = "Product name must be at most 100 characters")
  @CosmicWordCheck
  String name;

  @Size(max = 1000, message = "Product description must be at most 1000 characters")
  String description;

  @NotNull(message = "Price is required")
  @DecimalMin(value = "0.01", message = "Price must be at least 0.01")
  Float price;
}
