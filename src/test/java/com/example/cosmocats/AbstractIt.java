package com.example.cosmocats;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

@SpringBootTest
public abstract class AbstractIt {

    @RegisterExtension
    protected static WireMockExtension wireMockServer = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .configureStaticDsl(true)
            .build();

    @DynamicPropertySource
    static void setupTestContainerProperties(DynamicPropertyRegistry registry) {
        registry.add("application.inventory-service.base-url", wireMockServer::baseUrl);
        registry.add("application.pricing-service.base-url", wireMockServer::baseUrl);
        WireMock.configureFor(wireMockServer.getPort());
    }

    // Спрощені методи для stubbing - без зайвих деталей
    protected void stubInventoryCheck(UUID productId, boolean inStock, int quantity) {
        wireMockServer.stubFor(
            WireMock.get(WireMock.urlEqualTo("/api/inventory/" + productId))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(String.format(
                        "{\"productId\":\"%s\",\"inStock\":%s,\"quantity\":%d}",
                        productId, inStock, quantity
                    ))
            )
        );
    }

    protected void stubInventoryCheckError(UUID productId) {
        wireMockServer.stubFor(
            WireMock.get(WireMock.urlEqualTo("/api/inventory/" + productId))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(500)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"error\":\"Service unavailable\"}")
            )
        );
    }

    protected void stubPriceCalculation(UUID productId, double originalPrice, double discountedPrice) {
        wireMockServer.stubFor(
            WireMock.post(WireMock.urlEqualTo("/api/pricing/calculate"))
            .withRequestBody(
                WireMock.equalToJson(
                    String.format("{\"productId\":\"%s\",\"originalPrice\":%.2f}", productId, originalPrice)
                )
            )
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(String.format(
                        "{\"productId\":\"%s\",\"originalPrice\":%.2f,\"discountedPrice\":%.2f}",
                        productId, originalPrice, discountedPrice
                    ))
            )
        );
    }
}