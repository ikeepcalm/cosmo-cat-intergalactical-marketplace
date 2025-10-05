package net.cosmocat.marketplace.database.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Value;
import net.cosmocat.marketplace.validation.CosmicWordCheck;

import java.util.List;

@Value
@Schema(description = "Request object for creating a new category")
public class CategoryCreateDTO {

    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 50, message = "Category name must be between 2 and 50 characters")
    @CosmicWordCheck(message = "Category name must contain cosmic terms for our intergalactic marketplace")
    @Schema(description = "Category name", example = "Stellar Electronics")
    String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Schema(description = "Category description", example = "Electronic devices from across the galaxy")
    String description;

    @Size(max = 10, message = "Cannot have more than 10 tags")
    @Schema(description = "Category tags", example = "[\"electronics\", \"gadgets\", \"cosmic\"]")
    List<@NotBlank(message = "Tag cannot be blank")
    @Size(min = 2, max = 20, message = "Tag must be between 2 and 20 characters") String> tags;
}