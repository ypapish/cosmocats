package com.example.cosmocats.service;

import com.example.cosmocats.domain.CatInfo;
import com.example.cosmocats.featuretoggle.FeatureToggles;
import com.example.cosmocats.featuretoggle.annotation.FeatureToggle;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CosmoCatService {

    @FeatureToggle(FeatureToggles.COSMO_CATS)
    public List<CatInfo> getAllCatsInfos() {
        return List.of(
                CatInfo.builder()
                        .id(UUID.fromString("550e8400-e29b-41d4-a716-446655440010"))
                        .name("Cosmo Cat")
                        .description("The guardian of the cosmic realm")
                        .build(),
                CatInfo.builder()
                        .id(UUID.fromString("550e8400-e29b-41d4-a716-446655440011"))
                        .name("Astro Cat")
                        .description("Navigator of the stars and galaxies")
                        .build(),
                CatInfo.builder()
                        .id(UUID.fromString("550e8400-e29b-41d4-a716-446655440012"))
                        .name("Star Cat")
                        .description("Keeper of the celestial light")
                        .build(),
                CatInfo.builder()
                        .id(UUID.fromString("550e8400-e29b-41d4-a716-446655440013"))
                        .name("Nebula Cat")
                        .description("Mysterious dweller of cosmic clouds")
                        .build());
    }
}
