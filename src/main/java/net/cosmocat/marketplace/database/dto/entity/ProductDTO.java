package net.cosmocat.marketplace.database.dto.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import net.cosmocat.marketplace.database.entity.source.AvailabilityStatus;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    private String image;
    private Double price;
    private String currency;
    private Integer stockQuantity;
    private String sku;
    private AvailabilityStatus availabilityStatus;
    private Double weight;
    private String dimensions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private CategoryDTO category;
}