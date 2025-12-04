package net.cosmocat.marketplace.database.dto.entity;

import net.cosmocat.marketplace.database.entity.source.AvailabilityStatus;

import java.time.LocalDateTime;

public record ProductDTO(Long id, String name, String description, String image, Double price, String currency,
                         Integer stockQuantity, String sku, AvailabilityStatus availabilityStatus, Double weight,
                         String dimensions, LocalDateTime createdAt, LocalDateTime updatedAt, CategoryDTO category) {
}
