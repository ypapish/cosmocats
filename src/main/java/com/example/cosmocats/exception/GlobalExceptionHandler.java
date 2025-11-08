package com.example.cosmocats.exception;

import com.example.cosmocats.featuretoggle.exception.FeatureNotAvailableException;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail handleValidationExceptions(MethodArgumentNotValidException ex) {
    ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    problemDetail.setTitle("Validation Error");
    problemDetail.setProperty("timestamp", LocalDateTime.now());

    log.warn("Validation error: {}", ex.getMessage());
    return problemDetail;
  }

  @ExceptionHandler(ProductNotFoundException.class)
  public ProblemDetail handleProductNotFound(ProductNotFoundException ex) {
    ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
    problemDetail.setTitle("Product Not Found");
    problemDetail.setDetail(ex.getMessage());
    problemDetail.setProperty("timestamp", LocalDateTime.now());

    log.info("Product not found: {}", ex.getMessage());
    return problemDetail;
  }

  @ExceptionHandler(ProductAlreadyExistsException.class)
  public ProblemDetail handleProductAlreadyExists(ProductAlreadyExistsException ex) {
    ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.CONFLICT);
    problemDetail.setTitle("Product Already Exists");
    problemDetail.setDetail(ex.getMessage());
    problemDetail.setProperty("timestamp", LocalDateTime.now());

    log.info("Product already exists: {}", ex.getMessage());
    return problemDetail;
  }

  @ExceptionHandler(Exception.class)
  public ProblemDetail handleGenericException(Exception ex) {
    ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
    problemDetail.setTitle("Internal Server Error");
    problemDetail.setProperty("timestamp", LocalDateTime.now());

    log.error("Internal server error: {}", ex.getMessage(), ex);
    return problemDetail;
  }

  @ExceptionHandler(FeatureNotAvailableException.class)
  public ProblemDetail handleFeatureNotAvailable(FeatureNotAvailableException ex) {
    ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.SERVICE_UNAVAILABLE);
    problemDetail.setTitle("Feature Not Available");
    problemDetail.setDetail(ex.getMessage());
    problemDetail.setProperty("timestamp", LocalDateTime.now());

    log.info("Feature not available: {}", ex.getMessage());
    return problemDetail;
  }
}
