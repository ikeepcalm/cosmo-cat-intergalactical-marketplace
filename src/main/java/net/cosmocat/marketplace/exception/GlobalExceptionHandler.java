package net.cosmocat.marketplace.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ProblemDetail> handleValidationException(
      MethodArgumentNotValidException ex, HttpServletRequest request) {

    String traceId = UUID.randomUUID().toString();
    log.warn("Validation error [{}]: {}", traceId, ex.getMessage());

    List<Map<String, Object>> fieldErrors =
        ex.getBindingResult().getFieldErrors().stream()
            .map(
                fieldError -> {
                  Map<String, Object> error = new HashMap<>();
                  error.put("field", fieldError.getField());
                  error.put("rejectedValue", fieldError.getRejectedValue());
                  error.put("message", fieldError.getDefaultMessage());
                  error.put("code", fieldError.getCode());
                  return error;
                })
            .collect(Collectors.toList());

    String objectName = ex.getBindingResult().getObjectName();
    String mainMessage = String.format("Validation failed for object '%s'", objectName);

    if (!fieldErrors.isEmpty()) {
      Map<String, Object> firstError = fieldErrors.get(0);
      mainMessage =
          String.format(
              "Validation failed for object '%s': Field '%s' %s",
              objectName, firstError.get("field"), firstError.get("message"));
    }

    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, mainMessage);
    problemDetail.setTitle("Bad Request");
    problemDetail.setInstance(URI.create(request.getRequestURI()));
    problemDetail.setProperty("timestamp", Instant.now());
    problemDetail.setProperty("fieldErrors", fieldErrors);
    problemDetail.setProperty("traceId", traceId);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ProblemDetail> handleConstraintViolationException(
      ConstraintViolationException ex, HttpServletRequest request) {

    String traceId = UUID.randomUUID().toString();
    log.warn("Constraint violation [{}]: {}", traceId, ex.getMessage());

    List<Map<String, Object>> fieldErrors =
        ex.getConstraintViolations().stream()
            .map(
                violation -> {
                  String fieldName =
                      getFieldNameFromPropertyPath(violation.getPropertyPath().toString());
                  Map<String, Object> error = new HashMap<>();
                  error.put("field", fieldName);
                  error.put("rejectedValue", violation.getInvalidValue());
                  error.put("message", violation.getMessage());
                  error.put(
                      "code",
                      violation
                          .getConstraintDescriptor()
                          .getAnnotation()
                          .annotationType()
                          .getSimpleName());
                  return error;
                })
            .collect(Collectors.toList());

    String mainMessage = "Validation failed";
    if (!fieldErrors.isEmpty()) {
      Map<String, Object> firstError = fieldErrors.get(0);
      mainMessage =
          String.format(
              "Validation failed: Field '%s' %s",
              firstError.get("field"), firstError.get("message"));
    }

    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, mainMessage);
    problemDetail.setTitle("Bad Request");
    problemDetail.setInstance(URI.create(request.getRequestURI()));
    problemDetail.setProperty("timestamp", Instant.now());
    problemDetail.setProperty("fieldErrors", fieldErrors);
    problemDetail.setProperty("traceId", traceId);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ProblemDetail> handleIllegalArgumentException(
      IllegalArgumentException ex, HttpServletRequest request) {

    String traceId = UUID.randomUUID().toString();
    log.warn("Illegal argument [{}]: {}", traceId, ex.getMessage());

    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    problemDetail.setTitle("Bad Request");
    problemDetail.setInstance(URI.create(request.getRequestURI()));
    problemDetail.setProperty("timestamp", Instant.now());
    problemDetail.setProperty("traceId", traceId);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ProblemDetail> handleTypeMismatchException(
      MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

    String traceId = UUID.randomUUID().toString();
    log.warn("Type mismatch [{}]: {}", traceId, ex.getMessage());

    String message =
        String.format(
            "Invalid value '%s' for parameter '%s'. Expected type: %s",
            ex.getValue(), ex.getName(), ex.getRequiredType().getSimpleName());

    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, message);
    problemDetail.setTitle("Bad Request");
    problemDetail.setInstance(URI.create(request.getRequestURI()));
    problemDetail.setProperty("timestamp", Instant.now());
    problemDetail.setProperty("traceId", traceId);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ProblemDetail> handleHttpMessageNotReadableException(
      HttpMessageNotReadableException ex, HttpServletRequest request) {

    String traceId = UUID.randomUUID().toString();
    log.warn("Message not readable [{}]: {}", traceId, ex.getMessage());

    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST, "Invalid JSON format or malformed request body");
    problemDetail.setTitle("Bad Request");
    problemDetail.setInstance(URI.create(request.getRequestURI()));
    problemDetail.setProperty("timestamp", Instant.now());
    problemDetail.setProperty("traceId", traceId);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ProblemDetail> handleDataIntegrityViolationException(
      DataIntegrityViolationException ex, HttpServletRequest request) {

    String traceId = UUID.randomUUID().toString();
    log.warn("Data integrity violation [{}]: {}", traceId, ex.getMessage());

    String message = "Data integrity constraint violated";
    if (ex.getMessage().contains("duplicate key")) {
      message = "Duplicate entry - resource already exists";
    } else if (ex.getMessage().contains("foreign key")) {
      message = "Referenced resource does not exist";
    }

    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, message);
    problemDetail.setTitle("Conflict");
    problemDetail.setInstance(URI.create(request.getRequestURI()));
    problemDetail.setProperty("timestamp", Instant.now());
    problemDetail.setProperty("traceId", traceId);

    return ResponseEntity.status(HttpStatus.CONFLICT).body(problemDetail);
  }

  @ExceptionHandler(ResourceConflictException.class)
  public ResponseEntity<ProblemDetail> handleResourceConflictException(
      ResourceConflictException ex, HttpServletRequest request) {

    String traceId = UUID.randomUUID().toString();
    log.warn("Resource conflict [{}]: {}", traceId, ex.getMessage());

    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    problemDetail.setTitle("Conflict");
    problemDetail.setInstance(URI.create(request.getRequestURI()));
    problemDetail.setProperty("timestamp", Instant.now());
    problemDetail.setProperty("traceId", traceId);

    return ResponseEntity.status(HttpStatus.CONFLICT).body(problemDetail);
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ProblemDetail> handleResourceNotFoundException(
      ResourceNotFoundException ex, HttpServletRequest request) {

    String traceId = UUID.randomUUID().toString();
    log.warn("Resource not found [{}]: {}", traceId, ex.getMessage());

    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    problemDetail.setTitle("Not Found");
    problemDetail.setInstance(URI.create(request.getRequestURI()));
    problemDetail.setProperty("timestamp", Instant.now());
    problemDetail.setProperty("traceId", traceId);

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ProblemDetail> handleGenericException(
      Exception ex, HttpServletRequest request) {

    String traceId = UUID.randomUUID().toString();
    log.error("Unexpected error [{}]: {}", traceId, ex.getMessage(), ex);

    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected error occurred. Please contact support with trace ID: " + traceId);
    problemDetail.setTitle("Internal Server Error");
    problemDetail.setInstance(URI.create(request.getRequestURI()));
    problemDetail.setProperty("timestamp", Instant.now());
    problemDetail.setProperty("traceId", traceId);

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail);
  }

  private String getFieldNameFromPropertyPath(String propertyPath) {
    String[] parts = propertyPath.split("\\.");
    return parts[parts.length - 1];
  }
}
