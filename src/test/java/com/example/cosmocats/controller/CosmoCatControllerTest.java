package com.example.cosmocats.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.cosmocats.domain.CatInfo;
import com.example.cosmocats.dto.CatInfoDto;
import com.example.cosmocats.featuretoggle.FeatureToggleExtension;
import com.example.cosmocats.featuretoggle.FeatureToggles;
import com.example.cosmocats.featuretoggle.annotation.DisabledFeatureToggle;
import com.example.cosmocats.featuretoggle.annotation.EnabledFeatureToggle;
import com.example.cosmocats.service.CosmoCatService;
import com.example.cosmocats.service.mapper.CatInfoMapper;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CosmoCatController.class)
@ExtendWith({SpringExtension.class, FeatureToggleExtension.class})
class CosmoCatControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private CosmoCatService cosmoCatService;

  @Autowired
  private CatInfoMapper catInfoMapper;

  @Test
  @EnabledFeatureToggle(FeatureToggles.COSMO_CATS)
  void getCosmoCats_whenFeatureEnabled_shouldReturnCats() throws Exception {
    List<CatInfo> mockCatInfos =
        List.of(
            CatInfo.builder()
                .id(UUID.fromString("550e8400-e29b-41d4-a716-446655440010"))
                .name("Cosmo Cat")
                .description("The guardian of the cosmic realm")
                .build());

    List<CatInfoDto> mockCatDtos =
        List.of(
            CatInfoDto.builder()
                .id(UUID.fromString("550e8400-e29b-41d4-a716-446655440010"))
                .name("Cosmo Cat")
                .description("The guardian of the cosmic realm")
                .build());

    when(cosmoCatService.getAllCatsInfos()).thenReturn(mockCatInfos);
    when(catInfoMapper.toCatInfoDtoList(mockCatInfos)).thenReturn(mockCatDtos);

    mockMvc
        .perform(get("/api/v1/cosmo-cats"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].name").value("Cosmo Cat"));
  }

  @Test
  @DisabledFeatureToggle(FeatureToggles.COSMO_CATS)
  void getCosmoCats_whenFeatureDisabled_shouldReturnServiceUnavailable() throws Exception {
    mockMvc
        .perform(get("/api/v1/cosmo-cats"))
        .andExpect(status().isServiceUnavailable())
        .andExpect(jsonPath("$.title").value("Feature Not Available"))
        .andExpect(jsonPath("$.detail").value("Feature 'cosmoCats' is not available"));
  }

  @TestConfiguration
  static class TestConfig {
    @Bean
    @Primary
    public CosmoCatService cosmoCatService() {
      return mock(CosmoCatService.class);
    }

    @Bean
    @Primary
    public CatInfoMapper catInfoMapper() {
      return mock(CatInfoMapper.class);
    }
  }
}
