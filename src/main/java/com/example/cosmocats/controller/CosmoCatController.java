package com.example.cosmocats.controller;

import com.example.cosmocats.domain.CatInfo;
import com.example.cosmocats.dto.CatInfoDto;
import com.example.cosmocats.featuretoggle.FeatureToggles;
import com.example.cosmocats.featuretoggle.annotation.FeatureToggle;
import com.example.cosmocats.service.CosmoCatService;
import com.example.cosmocats.service.mapper.CatInfoMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cosmo-cats")
@RequiredArgsConstructor
@PreAuthorize("permitAll()")
public class CosmoCatController {

  private final CosmoCatService cosmoCatService;
  private final CatInfoMapper catInfoMapper;

  @GetMapping
  @FeatureToggle(FeatureToggles.COSMO_CATS)
  public ResponseEntity<List<CatInfoDto>> getCosmoCats() {
    List<CatInfo> catInfos = cosmoCatService.getAllCatsInfos();
    List<CatInfoDto> catInfoDtos = catInfos.stream().map(catInfoMapper::catInfoToDto).toList();
    return ResponseEntity.ok(catInfoDtos);
  }
}
