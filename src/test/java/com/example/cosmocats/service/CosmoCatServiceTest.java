package com.example.cosmocats.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.cosmocats.featuretoggle.FeatureToggleService;
import com.example.cosmocats.featuretoggle.exception.FeatureNotAvailableException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CosmoCatServiceTest {

  @Mock 
  private FeatureToggleService featureToggleService;
  
  @InjectMocks 
  private CosmoCatService cosmoCatService;

  @Test
  void getAllCatsInfos_whenFeatureEnabled_shouldReturnCats() {
    when(featureToggleService.check("cosmoCats")).thenReturn(true);

    var result = assertDoesNotThrow(() -> cosmoCatService.getAllCatsInfos());

    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(4, result.size());
    
    verify(featureToggleService, times(1)).check("cosmoCats");
  }

  @Test
  void getAllCatsInfos_whenFeatureDisabled_shouldThrowException() {
    when(featureToggleService.check("cosmoCats")).thenReturn(false);

    FeatureNotAvailableException exception = assertThrows(
        FeatureNotAvailableException.class, 
        () -> cosmoCatService.getAllCatsInfos()
    );
    
    assertEquals("Feature 'cosmoCats' is not available", exception.getMessage());
    
    verify(featureToggleService, times(1)).check("cosmoCats");
  }
}