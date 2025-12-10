package com.example.cosmocats.service.mapper;

import com.example.cosmocats.dto.product.ProductDto;
import com.example.cosmocats.dto.product.ProductListDto;
import com.example.cosmocats.dto.product.ProductUpdateDto;
import com.example.cosmocats.entity.ProductEntity;
import java.math.BigDecimal;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "productId", source = "productUuid")
    @Mapping(target = "category", source = "category.name")
    @Mapping(target = "price", expression = "java(product.getPrice().floatValue())")
    ProductDto toProductDto(ProductEntity product);

    default ProductListDto toProductListDto(List<ProductEntity> products) {
        return ProductListDto.builder().products(toProductDtoList(products)).build();
    }

    List<ProductDto> toProductDtoList(List<ProductEntity> products);

    @Mapping(target = "productId", ignore = true)
    @Mapping(target = "productUuid", expression = "java(java.util.UUID.randomUUID())")
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "price", expression = "java(java.math.BigDecimal.valueOf(productUpdateDto.getPrice()))")
    ProductEntity toProduct(ProductUpdateDto productUpdateDto);

    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "productUuid", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "price", expression = "java(java.math.BigDecimal.valueOf(productUpdateDto.getPrice()))")
    ProductEntity toProductWithId(Long productId, ProductUpdateDto productUpdateDto);

    default BigDecimal floatToBigDecimal(Float value) {
        return value != null ? BigDecimal.valueOf(value) : null;
    }

    default Float bigDecimalToFloat(BigDecimal value) {
        return value != null ? value.floatValue() : null;
    }
}