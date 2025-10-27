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
        WireMock.configureFor(wireMockServer.getPort());
    }

    // Додаткові методи для зручного stubbing
    protected void stubInventoryCheck(UUID productId, boolean inStock, int quantity) {
        wireMockServer.stubFor(
            com.github.tomakehurst.wiremock.client.WireMock.get(
                com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo("/api/inventory/" + productId)
            )
            .willReturn(
                com.github.tomakehurst.wiremock.client.WireMock.aResponse()
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
            com.github.tomakehurst.wiremock.client.WireMock.get(
                com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo("/api/inventory/" + productId)
            )
            .willReturn(
                com.github.tomakehurst.wiremock.client.WireMock.aResponse()
                    .withStatus(500)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"error\":\"Internal server error\"}")
            )
        );
    }

    protected void stubPriceCalculation(UUID productId, float originalPrice, float discountedPrice) {
        wireMockServer.stubFor(
            com.github.tomakehurst.wiremock.client.WireMock.post(
                com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo("/api/pricing/calculate")
            )
            .withRequestBody(
                com.github.tomakehurst.wiremock.client.WireMock.equalToJson(
                    String.format("{\"productId\":\"%s\",\"originalPrice\":%.2f}", productId, originalPrice)
                )
            )
            .willReturn(
                com.github.tomakehurst.wiremock.client.WireMock.aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(String.format(
                        "{\"productId\":\"%s\",\"originalPrice\":%.2f,\"discountedPrice\":%.2f,\"discountApplied\":%.2f}",
                        productId, originalPrice, discountedPrice, originalPrice - discountedPrice
                    ))
            )
        );
    }

    // Додатковий метод для прямого доступу до wireMockServer
    protected WireMockExtension getWireMockServer() {
        return wireMockServer;
    }
}