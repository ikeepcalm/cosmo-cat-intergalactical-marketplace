package net.cosmocat.marketplace.exception;

public class ResourceConflictException extends RuntimeException {

  public ResourceConflictException(String message) {
    super(message);
  }

  public ResourceConflictException(String message, Throwable cause) {
    super(message, cause);
  }

  public static ResourceConflictException forDuplicateField(
      String resourceType, String field, Object value) {
    return new ResourceConflictException(
        String.format("%s with %s '%s' already exists", resourceType, field, value));
  }
}