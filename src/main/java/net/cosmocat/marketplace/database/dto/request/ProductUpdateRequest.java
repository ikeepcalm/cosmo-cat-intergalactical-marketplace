package net.cosmocat.marketplace.database.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import net.cosmocat.marketplace.database.entity.source.AvailabilityStatus;
import net.cosmocat.marketplace.validation.CosmicWordCheck;

@Data
@Schema(description = "Request object for updating an existing product")
public class ProductUpdateRequest {

    @Size(min = 2, max = 100, message = "Product name must be between 2 and 100 characters")
    @CosmicWordCheck(required = false, message = "Product name should contain cosmic terms for our intergalactic marketplace")
    @Schema(description = "Product name", example = "Stellar Bluetooth Headphones Pro")
    private String name;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    @Schema(description = "Product description", example = "Premium wireless headphones with enhanced noise cancellation")
    private String description;

    @Positive(message = "Price must be positive")
    @DecimalMax(value = "999999.99", message = "Price cannot exceed 999,999.99")
    @DecimalMin(value = "0.01", message = "Price must be at least 0.01")
    @Schema(description = "Product price", example = "149.99")
    private Double price;

    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid 3-letter code (e.g., USD, EUR, GBP)")
    @Schema(description = "Currency code", example = "USD")
    private String currency;

    @Min(value = 0, message = "Stock quantity cannot be negative")
    @Max(value = 100000, message = "Stock quantity cannot exceed 100,000")
    @Schema(description = "Available stock quantity", example = "30")
    private Integer stockQuantity;

    @Size(min = 3, max = 50, message = "SKU must be between 3 and 50 characters")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "SKU must contain only uppercase letters, numbers, and hyphens")
    @Schema(description = "Stock Keeping Unit", example = "GALAXY-WBH-PRO-001")
    private String sku;

    @Pattern(regexp = "^(https?://.*|)$", message = "Image must be a valid URL or empty")
    @Schema(description = "Product image URL", example = "https://example.com/images/headphones-pro.jpg")
    private String image;

    @Positive(message = "Weight must be positive")
    @DecimalMax(value = "1000.0", message = "Weight cannot exceed 1000 kg")
    @DecimalMin(value = "0.001", message = "Weight must be at least 0.001 kg")
    @Schema(description = "Product weight in kg", example = "0.3")
    private Double weight;

    @Size(max = 100, message = "Dimensions cannot exceed 100 characters")
    @Pattern(regexp = "^\\d+(\\.\\d+)?\\s*x\\s*\\d+(\\.\\d+)?\\s*x\\s*\\d+(\\.\\d+)?\\s*(cm|mm|m|inch|in)$",
             message = "Dimensions must be in format 'LxWxH unit' (e.g., '22x20x9 cm')")
    @Schema(description = "Product dimensions", example = "22x20x9 cm")
    private String dimensions;

    @Schema(description = "Product availability status", example = "AVAILABLE")
    private AvailabilityStatus availabilityStatus;

    @Positive(message = "Category ID must be positive")
    @Schema(description = "Category ID", example = "1")
    private Long categoryId;
}