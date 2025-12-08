package org.utn.ba.order.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.utn.ba.order.exceptions.IdempotencyConflictException;

@ControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(IdempotencyConflictException.class)
  public ResponseEntity<?> idempotencyConflictExceptionHandler(Exception e) {
    return ResponseEntity
        .status(HttpStatus.CONFLICT)
        .body("The request is still being processed. Please retry after a moment.");
  }
}
