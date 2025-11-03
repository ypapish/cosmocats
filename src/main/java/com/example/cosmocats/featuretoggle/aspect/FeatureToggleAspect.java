package com.example.cosmocats.featuretoggle.aspect;

import com.example.cosmocats.featuretoggle.FeatureToggles;
import com.example.cosmocats.featuretoggle.annotation.FeatureToggle;
import com.example.cosmocats.featuretoggle.exception.FeatureNotAvailableException;
import com.example.cosmocats.featuretoggle.FeatureToggleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class FeatureToggleAspect {

    private final FeatureToggleService featureToggleService;

    @Around(value = "@annotation(featureToggle)")
    public Object checkFeatureToggleAnnotation(ProceedingJoinPoint joinPoint, FeatureToggle featureToggle) throws Throwable {
        return checkToggle(joinPoint, featureToggle);
    }

    private Object checkToggle(ProceedingJoinPoint joinPoint, FeatureToggle featureToggle) throws Throwable {
        FeatureToggles toggle = featureToggle.value();
        String featureName = toggle.getFeatureName();
        
        if (featureToggleService.check(featureName)) {
            return joinPoint.proceed();
        }
        
        log.warn("Feature toggle {} is not enabled!", featureName);
        throw new FeatureNotAvailableException(featureName);
    }
}