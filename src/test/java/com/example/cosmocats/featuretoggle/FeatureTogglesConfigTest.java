package com.example.cosmocats.featuretoggle;

import static org.junit.jupiter.api.Assertions.*;

import com.example.cosmocats.featuretoggle.config.FeatureToggleProperties;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FeatureTogglesConfigTest {

  private FeatureToggleProperties featureToggleProperties;

  @BeforeEach
  void setUp() {
    featureToggleProperties = new FeatureToggleProperties();
  }

  @Test
  void testFeatureTogglesEnum() {
    assertEquals(2, FeatureToggles.values().length);
    assertEquals("COSMO_CATS", FeatureToggles.COSMO_CATS.name());
    assertEquals("KITTY_PRODUCTS", FeatureToggles.KITTY_PRODUCTS.name());
  }

  @Test
  void constructor_ShouldCreateEmptyTogglesMap() {
    assertNull(featureToggleProperties.getToggles());
  }

  @Test
  void setToggles_ShouldUpdateTogglesMap() {
    Map<String, Boolean> testToggles = new HashMap<>();
    testToggles.put("cosmoCats", true);
    testToggles.put("kittyProducts", false);

    featureToggleProperties.setToggles(testToggles);

    assertEquals(testToggles, featureToggleProperties.getToggles());
    assertTrue(featureToggleProperties.getToggles().get("cosmoCats"));
    assertFalse(featureToggleProperties.getToggles().get("kittyProducts"));
  }

  @Test
  void check_ShouldReturnTrueWhenFeatureIsEnabled() {
    Map<String, Boolean> testToggles = new HashMap<>();
    testToggles.put("cosmoCats", true);
    featureToggleProperties.setToggles(testToggles);

    assertTrue(featureToggleProperties.check("cosmoCats"));
  }

  @Test
  void check_ShouldReturnFalseWhenFeatureIsDisabled() {
    Map<String, Boolean> testToggles = new HashMap<>();
    testToggles.put("kittyProducts", false);
    featureToggleProperties.setToggles(testToggles);

    assertFalse(featureToggleProperties.check("kittyProducts"));
  }

  @Test
  void check_ShouldReturnFalseWhenFeatureDoesNotExist() {
    Map<String, Boolean> testToggles = new HashMap<>();
    testToggles.put("cosmoCats", true);
    featureToggleProperties.setToggles(testToggles);

    assertFalse(featureToggleProperties.check("nonExistentFeature"));
  }

  @Test
  void check_ShouldReturnFalseWhenTogglesMapIsNull() {

    assertFalse(featureToggleProperties.check("anyFeature"));
  }

  @Test
  void getToggles_ShouldReturnCurrentTogglesMap() {
    Map<String, Boolean> testToggles = new HashMap<>();
    testToggles.put("newFeature", true);
    featureToggleProperties.setToggles(testToggles);

    assertEquals(testToggles, featureToggleProperties.getToggles());
  }
}
