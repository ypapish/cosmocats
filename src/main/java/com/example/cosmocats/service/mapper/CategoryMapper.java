package com.example.cosmocats.service.mapper;

import com.example.cosmocats.dto.CategoryDto;
import com.example.cosmocats.entity.CategoryEntity;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(target = "categoryId", source = "categoryUuid")
    @Mapping(target = "name", source = "name")
    CategoryDto toCategoryDto(CategoryEntity category);

    List<CategoryDto> toCategoryDtoList(List<CategoryEntity> categories);
}