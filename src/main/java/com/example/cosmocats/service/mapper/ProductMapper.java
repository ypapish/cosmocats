package com.example.cosmocats.service.mapper;

import com.example.cosmocats.domain.Product;
import com.example.cosmocats.dto.product.ProductDto;
import com.example.cosmocats.dto.product.ProductUpdateDto;
import com.example.cosmocats.dto.product.ProductListDto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.UUID;


@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "category", source = "category")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "price", source = "price")
    ProductDto toProductDto(Product product);

    default ProductListDto toProductListDto(List<Product> products) {
        return ProductListDto.builder()
                .products(toProductDtoList(products))
                .build();
    }

    List<ProductDto> toProductDtoList(List<Product> products);

    @Mapping(target = "productId", ignore = true)
    @Mapping(target = "category", source = "category")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "price", source = "price")
    Product toProduct(ProductUpdateDto productUpdateDto);

    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "category", source = "productUpdateDto.category")
    @Mapping(target = "name", source = "productUpdateDto.name")
    @Mapping(target = "description", source = "productUpdateDto.description") 
    @Mapping(target = "price", source = "productUpdateDto.price")
    Product toProductWithId(UUID productId, ProductUpdateDto productUpdateDto);
}