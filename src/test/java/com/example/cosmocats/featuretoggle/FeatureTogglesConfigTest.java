package com.example.cosmocats.featuretoggle;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "feature.cosmoCats.enabled=true",
    "feature.kittyProducts.enabled=false"
})
class FeatureTogglesConfigTest {

    @Test
    void testFeatureTogglesEnum() {
        assertEquals(2, FeatureToggles.values().length);
        assertEquals("COSMO_CATS", FeatureToggles.COSMO_CATS.name());
        assertEquals("KITTY_PRODUCTS", FeatureToggles.KITTY_PRODUCTS.name());
    }
}