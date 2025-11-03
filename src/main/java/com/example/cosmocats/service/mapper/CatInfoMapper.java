package com.example.cosmocats.service.mapper;

import com.example.cosmocats.domain.CatInfo;
import com.example.cosmocats.dto.CatInfoDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CatInfoMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "description", source = "description")
    CatInfoDto catInfoToDto(CatInfo catInfo);

    List<CatInfoDto> toCatInfoDtoList(List<CatInfo> catInfos);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "description", source = "description")
    CatInfo dtoToCatInfo(CatInfoDto catInfoDto);
}