package net.cosmocat.marketplace.database.dto;

import lombok.Data;
import net.cosmocat.marketplace.database.entity.Order;
import net.cosmocat.marketplace.database.entity.source.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class OrderDTO {
    private Long id;
    private OrderStatus status;
    private Long customerId;
    private List<OrderItemDTO> orderItems;
    private Double totalAmount;
    private String currency;
    private LocalDateTime orderDate;
    private String shippingAddress;
    private String billingAddress;

    public OrderDTO(Order order) {
        this.id = order.getId();
        this.status = order.getStatus();
        this.customerId = order.getCustomer() != null ? order.getCustomer().getId() : null;
        this.orderItems = order.getOrderItems() != null
            ? order.getOrderItems().stream().map(OrderItemDTO::new).collect(Collectors.toList())
            : null;
        this.totalAmount = order.getTotalAmount();
        this.currency = order.getCurrency();
        this.orderDate = order.getOrderDate();
        this.shippingAddress = order.getShippingAddress();
        this.billingAddress = order.getBillingAddress();
    }
}