package net.cosmocat.marketplace.exception.type;

public class CategoryNotFoundException extends ResourceNotFoundException {

  public CategoryNotFoundException(String message) {
    super(message);
  }

  public CategoryNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public static CategoryNotFoundException forId(Long id) {
    return new CategoryNotFoundException(String.format("Category not found with ID: %s", id));
  }
}