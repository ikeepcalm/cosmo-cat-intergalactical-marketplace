package net.cosmocat.marketplace.database.dto.entity;

import lombok.Data;
import net.cosmocat.marketplace.database.entity.OrderItem;

@Data
public class OrderItemDTO {
    private Long id;
    private Long orderId;
    private ProductDTO product;
    private Integer quantity;
    private Double priceAtTime;
    private String currency;
}