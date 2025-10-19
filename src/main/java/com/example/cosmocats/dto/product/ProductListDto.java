package com.example.cosmocats.dto.product;

import com.example.cosmocats.dto.product.ProductListDto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class ProductListDto {
  List<ProductDto> products;
}
