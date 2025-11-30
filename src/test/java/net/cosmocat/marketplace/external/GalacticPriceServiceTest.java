package net.cosmocat.marketplace.external;

import net.cosmocat.marketplace.wiremock.WireMockTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Test
    @DisplayName("Should handle service unavailable - DOWN status")
    void shouldHandleServiceDown() {
        // Given - API returns DOWN status
        wireMockServer.stubFor(get(urlEqualTo("/api/v1/health"))
                .willReturn(okJson("""
                        {
                          "status": "DOWN",
                          "service": "galactic-price-service"
                        }
                        """)));

        // When
        boolean result = galacticPriceService.isServiceAvailable();

        // Then - Verify our service correctly interprets DOWN status
        assertThat(result).isFalse();
        wireMockServer.verify(getRequestedFor(urlEqualTo("/api/v1/health")));
    }

    @Test
    @DisplayName("Should handle health check failure when endpoint returns error")
    void shouldHandleHealthCheckError() {
        // Given - API endpoint returns 500 error
        wireMockServer.stubFor(get(urlEqualTo("/api/v1/health"))
                .willReturn(serverError()));

        // When
        boolean result = galacticPriceService.isServiceAvailable();

        // Then - Verify our service handles errors gracefully
        assertThat(result).isFalse();
        wireMockServer.verify(getRequestedFor(urlEqualTo("/api/v1/health")));
    }

    @Test
    @DisplayName("Should handle health check when response is null")
    void shouldHandleNullHealthCheckResponse() {
        // Given - API returns empty/null response
        wireMockServer.stubFor(get(urlEqualTo("/api/v1/health"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("null")));

        // When
        boolean result = galacticPriceService.isServiceAvailable();

        // Then - Verify our service handles null response
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should handle different currency codes correctly")
    void shouldHandleDifferentCurrencyCodes() {
        // Given - API supports multiple currencies
        wireMockServer.stubFor(get(urlPathMatching("/api/v1/currency/convert.*"))
                .withQueryParam("from", equalTo("JPY"))
                .willReturn(okJson("""
                        {
                          "originalAmount": 1000.0,
                          "fromCurrency": "JPY",
                          "toCurrency": "GLC",
                          "convertedAmount": 3.5,
                          "exchangeRate": 0.0035
                        }
                        """)));

        // When
        Double result = galacticPriceService.convertToGalacticCredits(1000.0, "JPY");

        // Then
        assertThat(result).isEqualTo(3.5);

        // Verify we sent the correct currency code
        wireMockServer.verify(getRequestedFor(urlPathMatching("/api/v1/currency/convert.*"))
                .withQueryParam("from", equalTo("JPY")));
    }

    @Test
    @DisplayName("Should handle API errors during conversion")
    void shouldHandleApiErrorsDuringConversion() {
        // Given - API returns error
        wireMockServer.stubFor(get(urlPathMatching("/api/v1/currency/convert.*"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("Internal Server Error")));

        // When & Then - Verify our service wraps the error properly
        assertThatThrownBy(() -> galacticPriceService.convertToGalacticCredits(100.0, "USD"))
                .isInstanceOf(GalacticPriceService.ExternalApiException.class)
                .hasMessageContaining("Currency conversion failed");

        // Verify the request was made to the correct endpoint
        wireMockServer.verify(getRequestedFor(urlPathMatching("/api/v1/currency/convert.*")));
    }
}
