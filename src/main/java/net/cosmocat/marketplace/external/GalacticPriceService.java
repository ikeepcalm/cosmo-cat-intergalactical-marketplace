package net.cosmocat.marketplace.external;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
public class GalacticPriceService {

    private final RestClient restClient;

    public GalacticPriceService(
            RestClient.Builder restClientBuilder,
            @Value("${external.api.baseurl:http://localhost:8089}") String externalApiBaseUrl
    ) {
        this.restClient = restClientBuilder
                .baseUrl(externalApiBaseUrl)
                .build();
    }

    public Double convertToGalacticCredits(Double amount, String fromCurrency) {
        try {
            log.debug("Converting {} {} to GLC", amount, fromCurrency);

            CurrencyConversionResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/currency/convert")
                            .queryParam("amount", amount)
                            .queryParam("from", fromCurrency)
                            .queryParam("to", "GLC")
                            .build())
                    .retrieve()
                    .body(CurrencyConversionResponse.class);

            if (response == null) {
                throw new ExternalApiException("No response from currency conversion API");
            }

            log.info("Converted {} {} to {} GLC", amount, fromCurrency, response.convertedAmount());

            return response.convertedAmount();

        } catch (ExternalApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to convert currency: {}", e.getMessage(), e);
            throw new ExternalApiException("Currency conversion failed", e);
        }
    }

    public boolean isServiceAvailable() {
        try {
            log.debug("Checking service health");

            HealthCheckResponse response = restClient.get()
                    .uri("/api/v1/health")
                    .retrieve()
                    .body(HealthCheckResponse.class);

            return response != null && "UP".equals(response.status());

        } catch (Exception e) {
            log.warn("Service health check failed: {}", e.getMessage());
            return false;
        }
    }

    public record CurrencyConversionResponse(
            Double originalAmount,
            String fromCurrency,
            String toCurrency,
            Double convertedAmount,
            Double exchangeRate
    ) {
    }

    public record HealthCheckResponse(
            String status,
            String service
    ) {
    }

    public static class ExternalApiException extends RuntimeException {
        public ExternalApiException(String message) {
            super(message);
        }

        public ExternalApiException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}