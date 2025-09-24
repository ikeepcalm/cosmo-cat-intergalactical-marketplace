package net.cosmocat.marketplace.database.dto;

import lombok.Data;
import net.cosmocat.marketplace.database.entity.Product;
import net.cosmocat.marketplace.database.entity.source.AvailabilityStatus;

import java.time.LocalDateTime;

@Data
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

    public ProductDTO(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.description = product.getDescription();
        this.image = product.getImage();
        this.price = product.getPrice();
        this.currency = product.getCurrency();
        this.stockQuantity = product.getStockQuantity();
        this.sku = product.getSku();
        this.availabilityStatus = product.getAvailabilityStatus();
        this.weight = product.getWeight();
        this.dimensions = product.getDimensions();
        this.createdAt = product.getCreatedAt();
        this.updatedAt = product.getUpdatedAt();
        this.category = product.getCategory() != null ? new CategoryDTO(product.getCategory()) : null;
    }
}