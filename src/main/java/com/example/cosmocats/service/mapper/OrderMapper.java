package com.example.cosmocats.service.mapper;

import com.example.cosmocats.dto.order.OrderDto;
import com.example.cosmocats.dto.order.OrderEntryDto;
import com.example.cosmocats.entity.OrderEntity;
import com.example.cosmocats.entity.OrderEntryEntity;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
    componentModel = "spring",
    uses = {ProductMapper.class}
)
public interface OrderMapper {

    @Mapping(target = "id", source = "orderUuid")
    @Mapping(target = "entries", source = "entries")
    @Mapping(target = "totalPrice", expression = "java(order.getTotalPrice().doubleValue())")
    OrderDto toOrderDto(OrderEntity order);

    List<OrderDto> toOrderDtoList(List<OrderEntity> orders);

    @Mapping(target = "product", source = "product")
    @Mapping(target = "amount", source = "amount")
    OrderEntryDto toOrderEntryDto(OrderEntryEntity orderEntry);

    List<OrderEntryDto> toOrderEntryDtoList(List<OrderEntryEntity> orderEntries);
}