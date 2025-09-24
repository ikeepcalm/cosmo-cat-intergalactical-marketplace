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

    public OrderItemDTO(OrderItem orderItem) {
        this.id = orderItem.getId();
        this.orderId = orderItem.getOrder() != null ? orderItem.getOrder().getId() : null;
        this.product = orderItem.getProduct() != null ? new ProductDTO(orderItem.getProduct()) : null;
        this.quantity = orderItem.getQuantity();
        this.priceAtTime = orderItem.getPriceAtTime();
        this.currency = orderItem.getCurrency();
    }
}