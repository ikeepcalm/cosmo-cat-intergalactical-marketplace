package net.cosmocat.marketplace.database.dto.entity;

import lombok.Data;
import net.cosmocat.marketplace.database.entity.Cart;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class CartDTO {
    private Long id;
    private Long customerId;
    private List<CartItemDTO> cartItems;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}