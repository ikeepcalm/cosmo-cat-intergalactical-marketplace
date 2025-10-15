package net.cosmocat.marketplace.exception;

public abstract class ResourceConflictException extends RuntimeException {

  public ResourceConflictException(String message) {
    super(message);
  }

  public ResourceConflictException(String message, Throwable cause) {
    super(message, cause);
  }
}