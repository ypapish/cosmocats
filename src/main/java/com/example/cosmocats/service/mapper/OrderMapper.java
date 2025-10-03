package com.example.cosmocats.service.mapper;

import com.example.cosmocats.domain.order.Order;
import com.example.cosmocats.domain.order.OrderEntry;
import com.example.cosmocats.dto.order.OrderDto;
import com.example.cosmocats.dto.order.OrderEntryDto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;


@Mapper(componentModel = "spring", uses = {ProductMapper.class})
public interface OrderMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "entries", source = "entries")
    @Mapping(target = "totalPrice", source = "totalPrice")
    OrderDto toOrderDto(Order order);

    List<OrderDto> toOrderDtoList(List<Order> orders);

    @Mapping(target = "product", source = "product")
    @Mapping(target = "amount", source = "amount")
    OrderEntryDto toOrderEntryDto(OrderEntry orderEntry);

    List<OrderEntryDto> toOrderEntryDtoList(List<OrderEntry> orderEntries);
}