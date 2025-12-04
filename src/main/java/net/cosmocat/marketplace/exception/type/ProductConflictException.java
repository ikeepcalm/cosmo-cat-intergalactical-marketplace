package net.cosmocat.marketplace.exception.type;

public class ProductConflictException extends ResourceConflictException {

    public ProductConflictException(String message) {
        super(message);
    }

    public ProductConflictException(String message, Throwable cause) {
        super(message, cause);
    }

    public static ProductConflictException forDuplicateSku(String sku) {
        return new ProductConflictException(String.format("Product with SKU '%s' already exists", sku));
    }
}