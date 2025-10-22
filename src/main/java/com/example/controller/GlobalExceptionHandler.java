package com.example.controller;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity<ApiError> handleNotFound(EntityNotFoundException ex, HttpServletRequest req) {
    return build(HttpStatus.NOT_FOUND, ex.getMessage(), req.getRequestURI());
  }

  @ExceptionHandler(EntityExistsException.class)
  public ResponseEntity<ApiError> handleExists(EntityExistsException ex, HttpServletRequest req) {
    return build(HttpStatus.CONFLICT, ex.getMessage(), req.getRequestURI());
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiError> handleBadRequest(IllegalArgumentException ex, HttpServletRequest req) {
    return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req.getRequestURI());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
    String msg = ex.getBindingResult().getFieldErrors().stream()
      .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
      .findFirst().orElse("Validation failed");
    return build(HttpStatus.BAD_REQUEST, msg, req.getRequestURI());
  }

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<ApiError> handleOther(RuntimeException ex, HttpServletRequest req) {
    return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", req.getRequestURI());
  }

  private ResponseEntity<ApiError> build(HttpStatus status, String message, String path) {
    ApiError err = new ApiError(status.value(), status.getReasonPhrase(), message, path);
    return ResponseEntity.status(status).body(err);
  }
}


