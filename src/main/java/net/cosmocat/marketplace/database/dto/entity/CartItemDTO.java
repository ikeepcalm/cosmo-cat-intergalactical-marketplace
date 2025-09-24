package net.cosmocat.marketplace.database.dto.entity;

import lombok.Data;
import net.cosmocat.marketplace.database.entity.CartItem;

@Data
public class CartItemDTO {
    private Long id;
    private Long cartId;
    private ProductDTO product;
    private Integer quantity;

    public CartItemDTO(CartItem cartItem) {
        this.id = cartItem.getId();
        this.cartId = cartItem.getCart() != null ? cartItem.getCart().getId() : null;
        this.product = cartItem.getProduct() != null ? new ProductDTO(cartItem.getProduct()) : null;
        this.quantity = cartItem.getQuantity();
    }
}