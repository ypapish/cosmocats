package com.example.cosmocats.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CatInfoDto {
  @NotNull(message = "Cat id is required")
  UUID id;

  @NotBlank(message = "Cat name is required")
  @Size(min = 2, max = 50, message = "Cat name must be between 2 and 50 characters")
  String name;

  @Size(max = 200, message = "Description must be at most 200 characters")
  String description;
}
