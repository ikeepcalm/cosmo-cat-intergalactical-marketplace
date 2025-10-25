package net.cosmocat.marketplace.external;

import net.cosmocat.marketplace.wiremock.WireMockTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("GalacticPriceService WireMock Integration Tests")
class GalacticPriceServiceTest extends WireMockTestBase {

    private GalacticPriceService galacticPriceService;
    private RestTemplate restTemplate;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public RestTemplate testRestTemplate() {
            return new RestTemplateBuilder().build();
        }
    }

    @BeforeEach
    public void setUpService() {
        restTemplate = new RestTemplateBuilder().build();
        galacticPriceService = new GalacticPriceService(restTemplate, getWireMockBaseUrl());

        wireMockServer.resetAll();
    }

    @Nested
    @DisplayName("Currency Conversion Tests")
    class CurrencyConversionTests {

        @Test
        @DisplayName("Should successfully convert USD to Galactic Credits")
        void shouldConvertUsdToGalacticCredits() {
            // Given - Stub the external API endpoint
            wireMockServer.stubFor(get(urlPathEqualTo("/api/v1/currency/convert"))
                .withQueryParam("amount", equalTo("100.0"))
                .withQueryParam("from", equalTo("USD"))
                .withQueryParam("to", equalTo("GLC"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("""
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
            assertThat(result).isEqualTo(42.0);

            // Verify the request was made
            wireMockServer.verify(getRequestedFor(urlPathEqualTo("/api/v1/currency/convert"))
                .withQueryParam("amount", equalTo("100.0"))
                .withQueryParam("from", equalTo("USD")));
        }

        @Test
        @DisplayName("Should successfully convert EUR to Galactic Credits")
        void shouldConvertEurToGalacticCredits() {
            // Given
            wireMockServer.stubFor(get(urlPathMatching("/api/v1/currency/convert.*"))
                .withQueryParam("amount", equalTo("50.0"))
                .withQueryParam("from", equalTo("EUR"))
                .willReturn(okJson("""
                    {
                      "originalAmount": 50.0,
                      "fromCurrency": "EUR",
                      "toCurrency": "GLC",
                      "convertedAmount": 23.5,
                      "exchangeRate": 0.47
                    }
                    """)));

            // When
            Double result = galacticPriceService.convertToGalacticCredits(50.0, "EUR");

            // Then
            assertThat(result).isEqualTo(23.5);
        }

        @Test
        @DisplayName("Should handle different amounts correctly")
        void shouldHandleDifferentAmounts() {
            // Given - Multiple stubs for different amounts
            wireMockServer.stubFor(get(urlPathMatching("/api/v1/currency/convert.*"))
                .withQueryParam("amount", equalTo("1.0"))
                .willReturn(okJson("""
                    {
                      "originalAmount": 1.0,
                      "fromCurrency": "USD",
                      "toCurrency": "GLC",
                      "convertedAmount": 0.42,
                      "exchangeRate": 0.42
                    }
                    """)));

            wireMockServer.stubFor(get(urlPathMatching("/api/v1/currency/convert.*"))
                .withQueryParam("amount", equalTo("1000.0"))
                .willReturn(okJson("""
                    {
                      "originalAmount": 1000.0,
                      "fromCurrency": "USD",
                      "toCurrency": "GLC",
                      "convertedAmount": 420.0,
                      "exchangeRate": 0.42
                    }
                    """)));

            // When & Then
            assertThat(galacticPriceService.convertToGalacticCredits(1.0, "USD"))
                .isEqualTo(0.42);

            assertThat(galacticPriceService.convertToGalacticCredits(1000.0, "USD"))
                .isEqualTo(420.0);

            // Verify both requests were made
            wireMockServer.verify(2, getRequestedFor(urlPathMatching("/api/v1/currency/convert.*")));
        }

        @Test
        @DisplayName("Should throw exception when external API returns 500")
        void shouldThrowExceptionOnServerError() {
            // Given - Stub with server error
            wireMockServer.stubFor(get(urlPathMatching("/api/v1/currency/convert.*"))
                .willReturn(aResponse()
                    .withStatus(500)
                    .withBody("Internal Server Error")));

            // When & Then
            assertThatThrownBy(() ->
                galacticPriceService.convertToGalacticCredits(100.0, "USD"))
                .isInstanceOf(GalacticPriceService.ExternalApiException.class)
                .hasMessageContaining("Currency conversion failed");

            // Verify the request was attempted
            wireMockServer.verify(getRequestedFor(urlPathMatching("/api/v1/currency/convert.*")));
        }

        @Test
        @DisplayName("Should throw exception when external API returns 404")
        void shouldThrowExceptionOnNotFound() {
            // Given - Stub with 404
            wireMockServer.stubFor(get(urlPathMatching("/api/v1/currency/convert.*"))
                .willReturn(notFound()));

            // When & Then
            assertThatThrownBy(() ->
                galacticPriceService.convertToGalacticCredits(100.0, "USD"))
                .isInstanceOf(GalacticPriceService.ExternalApiException.class);
        }

        @Test
        @DisplayName("Should throw exception when external API times out")
        void shouldThrowExceptionOnTimeout() {
            // Given - Stub with delay exceeding timeout
            wireMockServer.stubFor(get(urlPathMatching("/api/v1/currency/convert.*"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withFixedDelay(15000) // 15 seconds delay (exceeds RestTemplate timeout)
                    .withBody("{}")));

            // When & Then
            assertThatThrownBy(() ->
                galacticPriceService.convertToGalacticCredits(100.0, "USD"))
                .isInstanceOf(GalacticPriceService.ExternalApiException.class);
        }

        @Test
        @DisplayName("Should throw exception when response is malformed")
        void shouldThrowExceptionOnMalformedResponse() {
            // Given - Stub with invalid JSON
            wireMockServer.stubFor(get(urlPathMatching("/api/v1/currency/convert.*"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{ invalid json")));

            // When & Then
            assertThatThrownBy(() ->
                galacticPriceService.convertToGalacticCredits(100.0, "USD"))
                .isInstanceOf(GalacticPriceService.ExternalApiException.class);
        }
    }

    @Nested
    @DisplayName("Health Check Tests")
    class HealthCheckTests {

        @Test
        @DisplayName("Should return true when service is available")
        void shouldReturnTrueWhenServiceIsUp() {
            // Given - Stub health endpoint
            wireMockServer.stubFor(get(urlEqualTo("/api/v1/health"))
                .willReturn(okJson("""
                    {
                      "status": "UP",
                      "service": "galactic-price-service"
                    }
                    """)));

            // When
            boolean isAvailable = galacticPriceService.isServiceAvailable();

            // Then
            assertThat(isAvailable).isTrue();

            // Verify the health check was called
            wireMockServer.verify(getRequestedFor(urlEqualTo("/api/v1/health")));
        }

        @Test
        @DisplayName("Should return false when service is down")
        void shouldReturnFalseWhenServiceIsDown() {
            // Given - Stub health endpoint with DOWN status
            wireMockServer.stubFor(get(urlEqualTo("/api/v1/health"))
                .willReturn(okJson("""
                    {
                      "status": "DOWN",
                      "service": "galactic-price-service"
                    }
                    """)));

            // When
            boolean isAvailable = galacticPriceService.isServiceAvailable();

            // Then
            assertThat(isAvailable).isFalse();
        }

        @Test
        @DisplayName("Should return false when health endpoint returns error")
        void shouldReturnFalseOnHealthCheckError() {
            // Given - Stub health endpoint with error
            wireMockServer.stubFor(get(urlEqualTo("/api/v1/health"))
                .willReturn(serverError()));

            // When
            boolean isAvailable = galacticPriceService.isServiceAvailable();

            // Then
            assertThat(isAvailable).isFalse();
        }

        @Test
        @DisplayName("Should return false when health endpoint is unreachable")
        void shouldReturnFalseWhenEndpointUnreachable() {
            // Given - No stub (endpoint doesn't exist)
            // WireMock will return 404 for unstubbed endpoints

            // When
            boolean isAvailable = galacticPriceService.isServiceAvailable();

            // Then
            assertThat(isAvailable).isFalse();
        }
    }

    @Nested
    @DisplayName("Advanced Stubbing Scenarios")
    class AdvancedStubbingTests {

        @Test
        @DisplayName("Should handle multiple consecutive calls with different responses")
        void shouldHandleMultipleCallsWithDifferentResponses() {
            // Given - Stub with scenarios for state-based testing
            wireMockServer.stubFor(get(urlPathMatching("/api/v1/currency/convert.*"))
                .inScenario("Conversion Scenario")
                .whenScenarioStateIs("Started")
                .withQueryParam("amount", equalTo("100.0"))
                .willReturn(okJson("""
                    {
                      "originalAmount": 100.0,
                      "fromCurrency": "USD",
                      "toCurrency": "GLC",
                      "convertedAmount": 42.0,
                      "exchangeRate": 0.42
                    }
                    """))
                .willSetStateTo("First Call Made"));

            wireMockServer.stubFor(get(urlPathMatching("/api/v1/currency/convert.*"))
                .inScenario("Conversion Scenario")
                .whenScenarioStateIs("First Call Made")
                .withQueryParam("amount", equalTo("100.0"))
                .willReturn(okJson("""
                    {
                      "originalAmount": 100.0,
                      "fromCurrency": "USD",
                      "toCurrency": "GLC",
                      "convertedAmount": 45.0,
                      "exchangeRate": 0.45
                    }
                    """)));

            // When & Then - First call
            Double firstResult = galacticPriceService.convertToGalacticCredits(100.0, "USD");
            assertThat(firstResult).isEqualTo(42.0);

            // When & Then - Second call (different rate due to scenario state)
            Double secondResult = galacticPriceService.convertToGalacticCredits(100.0, "USD");
            assertThat(secondResult).isEqualTo(45.0);

            // Verify both calls were made
            wireMockServer.verify(2, getRequestedFor(urlPathMatching("/api/v1/currency/convert.*")));
        }

        @Test
        @DisplayName("Should verify request headers if authentication is added")
        void shouldVerifyRequestHeaders() {
            // Given - Stub that accepts any headers
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
            galacticPriceService.convertToGalacticCredits(100.0, "USD");

            // Then - Verify request was made (demonstrates header verification capability)
            // In real scenarios with authentication, you would verify Authorization header:
            // .withHeader("Authorization", equalTo("Bearer token"))
            wireMockServer.verify(getRequestedFor(urlPathMatching("/api/v1/currency/convert.*")));
        }
    }
}