package net.cosmocat.marketplace.database.dto.entity;

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
}