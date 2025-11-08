package net.cosmocat.marketplace.wiremock;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
public abstract class WireMockTestBase {

    protected static WireMockServer wireMockServer;

    @BeforeAll
    public static void startWireMock() {
        wireMockServer = new WireMockServer(
                WireMockConfiguration.options()
                        .dynamicPort()
        );
        wireMockServer.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("external.api.baseurl", () -> "http://localhost:" + wireMockServer.port());
    }

    @AfterAll
    public static void stopWireMock() {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
        }
    }

    protected String getWireMockBaseUrl() {
        return "http://localhost:" + wireMockServer.port();
    }

    protected int getWireMockPort() {
        return wireMockServer.port();
    }
}