package net.cosmocat.marketplace.exception;

public class CategoryConflictException extends ResourceConflictException {

  public CategoryConflictException(String message) {
    super(message);
  }

  public CategoryConflictException(String message, Throwable cause) {
    super(message, cause);
  }

  public static CategoryConflictException forDuplicateName(String name) {
    return new CategoryConflictException(
        String.format("Category with name '%s' already exists", name));
  }
}