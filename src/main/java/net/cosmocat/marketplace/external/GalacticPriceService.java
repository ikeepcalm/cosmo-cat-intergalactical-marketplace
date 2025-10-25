package net.cosmocat.marketplace.external;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class GalacticPriceService {

    private final RestTemplate restTemplate;
    private final String externalApiBaseUrl;

    public GalacticPriceService(
        RestTemplate restTemplate,
        @Value("${external.api.baseurl:http://localhost:8089}") String externalApiBaseUrl
    ) {
        this.restTemplate = restTemplate;
        this.externalApiBaseUrl = externalApiBaseUrl;
    }

    public Double convertToGalacticCredits(Double amount, String fromCurrency) {
        try {
            String url = String.format(
                "%s/api/v1/currency/convert?amount=%s&from=%s&to=GLC",
                externalApiBaseUrl, amount, fromCurrency
            );

            log.debug("Calling external API: {}", url);

            CurrencyConversionResponse response = restTemplate.getForObject(
                url,
                CurrencyConversionResponse.class
            );

            if (response == null) {
                throw new ExternalApiException("No response from currency conversion API");
            }

            log.info("Converted {} {} to {} GLC", amount, fromCurrency, response.convertedAmount());

            return response.convertedAmount();

        } catch (Exception e) {
            log.error("Failed to convert currency: {}", e.getMessage(), e);
            throw new ExternalApiException("Currency conversion failed", e);
        }
    }

    public boolean isServiceAvailable() {
        try {
            String url = externalApiBaseUrl + "/api/v1/health";
            log.debug("Checking service health: {}", url);

            HealthCheckResponse response = restTemplate.getForObject(
                url,
                HealthCheckResponse.class
            );

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
    ) {}

    public record HealthCheckResponse(
        String status,
        String service
    ) {}

    public static class ExternalApiException extends RuntimeException {
        public ExternalApiException(String message) {
            super(message);
        }

        public ExternalApiException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}