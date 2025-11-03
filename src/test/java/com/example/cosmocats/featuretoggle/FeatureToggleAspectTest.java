package com.example.cosmocats.featuretoggle;

import com.example.cosmocats.featuretoggle.annotation.FeatureToggle;
import com.example.cosmocats.featuretoggle.aspect.FeatureToggleAspect;
import com.example.cosmocats.featuretoggle.exception.FeatureNotAvailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeatureToggleAspectTest {

    @Mock
    private FeatureToggleService featureToggleService;

    private FeatureToggleAspect featureToggleAspect;
    private TestService testServiceProxy;

    @BeforeEach
    void setup() {
        featureToggleAspect = new FeatureToggleAspect(featureToggleService);

        TestService testService = new TestService();
        AspectJProxyFactory proxyFactory = new AspectJProxyFactory(testService);
        proxyFactory.addAspect(featureToggleAspect);
        testServiceProxy = proxyFactory.getProxy();
    }

    @Test
    void shouldAllowMethodExecutionWhenFeatureIsEnabled() {
        // Given
        when(featureToggleService.check("cosmoCats")).thenReturn(true);

        // When
        String result = testServiceProxy.getCosmoCatsFeature();

        // Then
        assertEquals("Feature executed", result);
        verify(featureToggleService, times(1)).check("cosmoCats");
    }

    @Test
    void shouldBlockMethodExecutionWhenFeatureIsDisabled() {
        // Given
        when(featureToggleService.check("cosmoCats")).thenReturn(false);

        // When & Then
        FeatureNotAvailableException exception = assertThrows(FeatureNotAvailableException.class, () -> {
            testServiceProxy.getCosmoCatsFeature();
        });

        assertEquals("Feature 'cosmoCats' is not available", exception.getMessage());
    }

    static class TestService {
        @FeatureToggle(FeatureToggles.COSMO_CATS)
        public String getCosmoCatsFeature() {
            return "Feature executed";
        }
    }
}