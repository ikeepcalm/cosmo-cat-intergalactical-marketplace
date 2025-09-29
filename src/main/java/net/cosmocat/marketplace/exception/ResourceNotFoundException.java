package net.cosmocat.marketplace.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public static ResourceNotFoundException forId(String resourceType, Long id) {
        return new ResourceNotFoundException(String.format("%s not found with ID: %s", resourceType, id));
    }

    public static ResourceNotFoundException forField(String resourceType, String field, Object value) {
        return new ResourceNotFoundException(String.format("%s not found with %s: %s", resourceType, field, value));
    }
}