package com.example.cosmocats.service.mapper;

import com.example.cosmocats.domain.Cart;
import com.example.cosmocats.dto.CartDto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;


@Mapper(componentModel = "spring", uses = {ProductMapper.class})
public interface CartMapper {

    @Mapping(target = "cartId", source = "cartId")
    @Mapping(target = "customerId", source = "customerId")
    @Mapping(target = "products", source = "products")
    @Mapping(target = "totalPrice", source = "totalPrice")
    CartDto toCartDto(Cart cart);

    List<CartDto> toCartDtoList(List<Cart> carts);

    @Mapping(target = "cartId", source = "cartId")
    @Mapping(target = "customerId", source = "customerId")
    @Mapping(target = "products", source = "products")
    @Mapping(target = "totalPrice", source = "totalPrice")
    Cart toCart(CartDto cartDto);
}