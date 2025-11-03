package com.example.cosmocats.dto.product;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ProductListDto {
  List<ProductDto> products;
}
