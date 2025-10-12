package net.cosmocat.marketplace.database.dto.entity;

import java.time.LocalDateTime;
import lombok.Value;
import net.cosmocat.marketplace.database.entity.source.AvailabilityStatus;

@Value
public class ProductDTO {
  Long id;
  String name;
  String description;
  String image;
  Double price;
  String currency;
  Integer stockQuantity;
  String sku;
  AvailabilityStatus availabilityStatus;
  Double weight;
  String dimensions;
  LocalDateTime createdAt;
  LocalDateTime updatedAt;
  CategoryDTO category;
}
