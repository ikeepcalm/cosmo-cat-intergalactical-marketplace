package net.cosmocat.marketplace.database.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "RFC 9457 compliant error response")
public class ErrorResponse {

    @Schema(description = "HTTP status code", example = "400")
    private int status;

    @Schema(description = "Error type/category", example = "Bad Request")
    private String error;

    @Schema(description = "Human-readable error message", example = "Validation failed for object 'ProductDTO': Field 'price' must be greater than 0.")
    private String message;

    @Schema(description = "API path where error occurred", example = "/api/v1/products")
    private String path;

    @Schema(description = "Timestamp when error occurred")
    private LocalDateTime timestamp;

    @Schema(description = "Detailed validation errors for fields")
    private List<FieldError> fieldErrors;

    @Schema(description = "Request ID for tracing")
    private String traceId;

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Field-specific validation error")
    public static class FieldError {

        @Schema(description = "Field name", example = "price")
        private String field;

        @Schema(description = "Rejected value", example = "-10.0")
        private Object rejectedValue;

        @Schema(description = "Validation error message", example = "Price must be positive")
        private String message;

        @Schema(description = "Error code", example = "Positive")
        private String code;
    }
}