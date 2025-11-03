package com.example.cosmocats.featuretoggle.annotation;

import com.example.cosmocats.featuretoggle.FeatureToggles;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface DisabledFeatureToggle {
    FeatureToggles value();
}