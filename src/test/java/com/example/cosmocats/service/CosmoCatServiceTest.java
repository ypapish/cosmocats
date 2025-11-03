package com.example.cosmocats.service;

import com.example.cosmocats.featuretoggle.FeatureToggleService;
import com.example.cosmocats.featuretoggle.config.FeatureToggleProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeatureToggleServiceTest {

    @Mock
    private FeatureToggleProperties featureToggleProperties;

    private FeatureToggleService featureToggleService;

    @BeforeEach
    void setUp() {
        when(featureToggleProperties.getToggles()).thenReturn(new HashMap<>());
        featureToggleService = new FeatureToggleService(featureToggleProperties);
    }

    @Test
    void constructor_ShouldInitializeFeaturesCorrectly() {
        Map<String, Boolean> testToggles = new HashMap<>();
        testToggles.put("cosmoCats", true);
        testToggles.put("kittyProducts", false);

        when(featureToggleProperties.getToggles()).thenReturn(testToggles);

        FeatureToggleService service = new FeatureToggleService(featureToggleProperties);

        assertTrue(service.check("cosmoCats"));
        assertFalse(service.check("kittyProducts"));
    }

    @Test
    void enable_ShouldEnableFeature() {
        featureToggleService.enable("cosmoCats");

        assertTrue(featureToggleService.check("cosmoCats"));
    }

    @Test
    void disable_ShouldDisableFeature() {
        featureToggleService.enable("cosmoCats");
        featureToggleService.disable("cosmoCats");

        assertFalse(featureToggleService.check("cosmoCats"));
    }

    @Test
    void check_WhenFeatureDoesNotExist_ShouldReturnFalse() {
        assertFalse(featureToggleService.check("randomNonExistentFeature"));
    }
}