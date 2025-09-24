package net.cosmocat.marketplace.database.dto.request;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import net.cosmocat.marketplace.database.entity.source.AvailabilityStatus;

@Data
@Schema(description = "Request object for creating a new product")
public class ProductCreateRequest {

    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 100, message = "Product name must be between 2 and 100 characters")
    @Schema(description = "Product name", example = "Wireless Bluetooth Headphones")
    private String name;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    @Schema(description = "Product description", example = "High-quality wireless headphones with noise cancellation")
    private String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    @Schema(description = "Product price", example = "129.99")
    private Double price;

    @NotBlank(message = "Currency is required")
    @jakarta.validation.constraints.Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid 3-letter code")
    @Schema(description = "Currency code", example = "USD")
    private String currency;

    @Min(value = 0, message = "Stock quantity cannot be negative")
    @Schema(description = "Available stock quantity", example = "50")
    private Integer stockQuantity;

    @Size(max = 50, message = "SKU cannot exceed 50 characters")
    @Schema(description = "Stock Keeping Unit", example = "WBH-001")
    private String sku;

    @Pattern(regexp = "^(https?://.*|)$", message = "Image must be a valid URL or empty")
    @Schema(description = "Product image URL", example = "https://example.com/images/headphones.jpg")
    private String image;

    @Positive(message = "Weight must be positive")
    @Schema(description = "Product weight in kg", example = "0.25")
    private Double weight;

    @Size(max = 100, message = "Dimensions cannot exceed 100 characters")
    @Schema(description = "Product dimensions", example = "20x18x8 cm")
    private String dimensions;

    @Schema(description = "Product availability status", example = "AVAILABLE")
    private AvailabilityStatus availabilityStatus;

    @Schema(description = "Category ID", example = "1")
    private Long categoryId;
}