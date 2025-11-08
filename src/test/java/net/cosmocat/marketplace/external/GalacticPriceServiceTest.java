package net.cosmocat.marketplace.external;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

import net.cosmocat.marketplace.wiremock.WireMockTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("GalacticPriceService Integration Tests")
class GalacticPriceServiceTest extends WireMockTestBase {

    @Autowired
    private GalacticPriceService galacticPriceService;

    @BeforeEach
    void setUp() {
        wireMockServer.resetAll();
    }

    @Test
    @DisplayName("Should send GET request to correct endpoint with proper query parameters")
    void shouldSendCorrectRequestForCurrencyConversion() {
        // Given
        wireMockServer.stubFor(get(urlPathEqualTo("/api/v1/currency/convert"))
            .willReturn(okJson("""
                {
                  "originalAmount": 100.0,
                  "fromCurrency": "USD",
                  "toCurrency": "GLC",
                  "convertedAmount": 42.0,
                  "exchangeRate": 0.42
                }
                """)));

        // When
        galacticPriceService.convertToGalacticCredits(100.0, "USD");

        // Then
        wireMockServer.verify(getRequestedFor(urlPathEqualTo("/api/v1/currency/convert"))
            .withQueryParam("amount", equalTo("100.0"))
            .withQueryParam("from", equalTo("USD"))
            .withQueryParam("to", equalTo("GLC")));
    }

    @Test
    @DisplayName("Should correctly parse API response and extract converted amount")
    void shouldParseApiResponseCorrectly() {
        // Given
        wireMockServer.stubFor(get(urlPathMatching("/api/v1/currency/convert.*"))
            .willReturn(okJson("""
                {
                  "originalAmount": 250.0,
                  "fromCurrency": "EUR",
                  "toCurrency": "GLC",
                  "convertedAmount": 117.5,
                  "exchangeRate": 0.47
                }
                """)));

        // When
        Double result = galacticPriceService.convertToGalacticCredits(250.0, "EUR");

        // Then
        assertThat(result).isEqualTo(117.5);

        // Verify
        wireMockServer.verify(getRequestedFor(urlPathMatching("/api/v1/currency/convert.*"))
            .withQueryParam("amount", equalTo("250.0"))
            .withQueryParam("from", equalTo("EUR")));
    }

    @Test
    @DisplayName("Should send GET request to health endpoint with correct path")
    void shouldCheckHealthAtCorrectEndpoint() {
        // Given
        wireMockServer.stubFor(get(urlEqualTo("/api/v1/health"))
            .willReturn(okJson("""
                {
                  "status": "UP",
                  "service": "galactic-price-service"
                }
                """)));

        // When
        boolean result = galacticPriceService.isServiceAvailable();

        // Then
        assertThat(result).isTrue();
        wireMockServer.verify(getRequestedFor(urlEqualTo("/api/v1/health")));
    }

    @Test
    @DisplayName("Should validate API contract - response structure matches our expectations")
    void shouldValidateApiContract() {
        // Given
        wireMockServer.stubFor(get(urlPathMatching("/api/v1/currency/convert.*"))
            .willReturn(okJson("""
                {
                  "originalAmount": 100.0,
                  "fromCurrency": "USD",
                  "toCurrency": "GLC",
                  "convertedAmount": 42.0,
                  "exchangeRate": 0.42
                }
                """)));

        // When
        Double result = galacticPriceService.convertToGalacticCredits(100.0, "USD");

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(42.0);

        // Verify
        wireMockServer.verify(getRequestedFor(urlPathMatching("/api/v1/currency/convert.*"))
            .withQueryParam("amount", matching("\\d+\\.\\d+"))  // Validates we send decimal format
            .withQueryParam("from", matching("[A-Z]{3}"))       // Validates we send 3-letter currency code
            .withQueryParam("to", equalTo("GLC")));             // Validates we always convert to GLC
    }
}
