package com.example.cosmocats.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.example.cosmocats.featuretoggle.FeatureToggleService;
import com.example.cosmocats.featuretoggle.config.FeatureToggleProperties;
import com.example.cosmocats.featuretoggle.exception.FeatureNotAvailableException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FeatureToggleServiceTest {

  @Mock private FeatureToggleProperties featureToggleProperties;
  @Mock private FeatureToggleService featureToggleService;

  private FeatureToggleService featureToggleServiceInstance;

  @InjectMocks private CosmoCatService cosmoCatService;

  @BeforeEach
  void setUp() {
    when(featureToggleProperties.getToggles()).thenReturn(new HashMap<>());
    featureToggleServiceInstance = new FeatureToggleService(featureToggleProperties);
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
    featureToggleServiceInstance.enable("cosmoCats");

    assertTrue(featureToggleServiceInstance.check("cosmoCats"));
  }

  @Test
  void disable_ShouldDisableFeature() {
    featureToggleServiceInstance.enable("cosmoCats");
    featureToggleServiceInstance.disable("cosmoCats");

    assertFalse(featureToggleServiceInstance.check("cosmoCats"));
  }

  @Test
  void check_WhenFeatureDoesNotExist_ShouldReturnFalse() {
    assertFalse(featureToggleServiceInstance.check("randomNonExistentFeature"));
  }

  @Test
  void getAllCatsInfos_whenFeatureEnabled_shouldReturnCats() {
    when(featureToggleService.check("cosmoCats")).thenReturn(true);

    cosmoCatService.getAllCatsInfos();
  }

  @Test
  void getAllCatsInfos_whenFeatureDisabled_shouldThrowException() {
    when(featureToggleService.check("cosmoCats")).thenReturn(false);

    assertThrows(FeatureNotAvailableException.class, () -> cosmoCatService.getAllCatsInfos());
  }
}
