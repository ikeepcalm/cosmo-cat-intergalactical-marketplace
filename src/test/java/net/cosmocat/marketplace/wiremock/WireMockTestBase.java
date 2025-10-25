package net.cosmocat.marketplace.wiremock;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public abstract class WireMockTestBase {

    protected WireMockServer wireMockServer;
    protected static final int WIREMOCK_PORT = 8089;

    @BeforeEach
    public void startWireMock() {
        wireMockServer = new WireMockServer(
            WireMockConfiguration.options()
                .port(WIREMOCK_PORT)
                .dynamicPort()
        );
        wireMockServer.start();

        System.setProperty("wiremock.server.baseUrl",
            "http://localhost:" + wireMockServer.port());
    }

    @AfterEach
    public void stopWireMock() {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
        }
        System.clearProperty("wiremock.server.baseUrl");
    }

    protected String getWireMockBaseUrl() {
        return "http://localhost:" + wireMockServer.port();
    }

    protected int getWireMockPort() {
        return wireMockServer.port();
    }
}