package com.example.cosmocats.featuretoggle.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.SERVICE_UNAVAILABLE)
public class FeatureNotAvailableException extends RuntimeException {

    private static final String FEATURE_NOT_AVAILABLE = "Feature '%s' is not available";

    public FeatureNotAvailableException(String featureName) {
        super(String.format(FEATURE_NOT_AVAILABLE, featureName));
    }
}
