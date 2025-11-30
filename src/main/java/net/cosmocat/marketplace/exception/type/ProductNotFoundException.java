package net.cosmocat.marketplace.exception.type;

public class ProductNotFoundException extends ResourceNotFoundException {

    public ProductNotFoundException(String message) {
        super(message);
    }

    public ProductNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public static ProductNotFoundException forId(Long id) {
        return new ProductNotFoundException(String.format("Product not found with ID: %s", id));
    }
}