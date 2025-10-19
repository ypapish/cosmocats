package com.example.cosmocats.service.mapper;

import com.example.cosmocats.domain.Category;
import com.example.cosmocats.dto.CategoryDto;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

  CategoryDto toCategoryDto(Category category);

  List<CategoryDto> toCategoryDtoList(List<Category> categories);
}
