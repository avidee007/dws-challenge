package com.dws.challenge.exception;

import com.dws.challenge.web.ErrorResponse;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler for any exception happening in API execution.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  public static final String FAILURE = "Failure";

  /**
   * Handles the {@code TransferAmountException} occurred in a transfer process.
   *
   * @param ex Throwable object containing exception details.
   * @return ResponseEntity with status, status code, error details and timestamp.
   */
  @ExceptionHandler(TransferAmountException.class)
  public ResponseEntity<ErrorResponse> handleTransferAmountException(TransferAmountException ex) {

    var errorResponse = new ErrorResponse(FAILURE, HttpStatus.INTERNAL_SERVER_ERROR.value(),
        ex.getMessage(), Instant.now());
    return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  /**
   * Handles the {@code AccountNotFoundException} occurred in a transfer process.
   *
   * @param ex Throwable object containing exception details.
   * @return ResponseEntity with status, status code, error details and timestamp.
   */
  @ExceptionHandler(AccountNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleAccountNotFoundException(AccountNotFoundException ex) {

    var errorResponse = new ErrorResponse(FAILURE, HttpStatus.INTERNAL_SERVER_ERROR.value(),
        ex.getMessage(), Instant.now());
    return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  /**
   * Handles the {@code IllegalArgumentException} occurred in a transfer process.
   *
   * @param ex Throwable object containing exception details.
   * @return ResponseEntity with status, status code, error details and timestamp.
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {

    var errorResponse = new ErrorResponse(FAILURE, HttpStatus.BAD_REQUEST.value(),
        ex.getMessage(), Instant.now());
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }
}