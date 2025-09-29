package net.cosmocat.marketplace.database.dto.request;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import net.cosmocat.marketplace.database.entity.source.AvailabilityStatus;
import net.cosmocat.marketplace.validation.CosmicWordCheck;

@Data
@Schema(description = "Request object for creating a new product")
public class ProductCreateRequest {

    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 100, message = "Product name must be between 2 and 100 characters")
    @CosmicWordCheck(message = "Product name must contain cosmic terms for our intergalactic marketplace")
    @Schema(description = "Product name", example = "Stellar Bluetooth Headphones")
    private String name;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    @Schema(description = "Product description", example = "High-quality cosmic headphones with stellar noise cancellation technology")
    private String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    @DecimalMax(value = "999999.99", message = "Price cannot exceed 999,999.99")
    @DecimalMin(value = "0.01", message = "Price must be at least 0.01")
    @Schema(description = "Product price", example = "129.99")
    private Double price;

    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid 3-letter code (e.g., USD, EUR, GBP)")
    @Schema(description = "Currency code", example = "USD")
    private String currency;

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    @Max(value = 100000, message = "Stock quantity cannot exceed 100,000")
    @Schema(description = "Available stock quantity", example = "50")
    private Integer stockQuantity;

    @Size(min = 3, max = 50, message = "SKU must be between 3 and 50 characters")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "SKU must contain only uppercase letters, numbers, and hyphens")
    @Schema(description = "Stock Keeping Unit", example = "STAR-WBH-001")
    private String sku;

    @Pattern(regexp = "^(https?://.*|)$", message = "Image must be a valid URL or empty")
    @Schema(description = "Product image URL", example = "https://example.com/images/headphones.jpg")
    private String image;

    @Positive(message = "Weight must be positive")
    @DecimalMax(value = "1000.0", message = "Weight cannot exceed 1000 kg")
    @DecimalMin(value = "0.001", message = "Weight must be at least 0.001 kg")
    @Schema(description = "Product weight in kg", example = "0.25")
    private Double weight;

    @Size(max = 100, message = "Dimensions cannot exceed 100 characters")
    @Pattern(regexp = "^\\d+(\\.\\d+)?\\s*x\\s*\\d+(\\.\\d+)?\\s*x\\s*\\d+(\\.\\d+)?\\s*(cm|mm|m|inch|in)$",
             message = "Dimensions must be in format 'LxWxH unit' (e.g., '20x18x8 cm')")
    @Schema(description = "Product dimensions", example = "20x18x8 cm")
    private String dimensions;

    @NotNull(message = "Availability status is required")
    @Schema(description = "Product availability status", example = "AVAILABLE")
    private AvailabilityStatus availabilityStatus;

    @Positive(message = "Category ID must be positive")
    @Schema(description = "Category ID", example = "1")
    private Long categoryId;
}