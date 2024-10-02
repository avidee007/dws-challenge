package com.dws.challenge.exception;

import com.dws.challenge.web.ErrorResponse;
import java.util.Objects;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {
  private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

  @Test
  void handleTransferAmountException() {

    var transferException = new TransferAmountException("Transfer exception happened");
    ResponseEntity<ErrorResponse> responseEntity =
        exceptionHandler.handleTransferAmountException(transferException);

    Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
    Assertions.assertEquals("Transfer exception happened", Objects.requireNonNull(
        responseEntity.getBody()).error());
    Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), responseEntity.getBody().code());
  }

  @Test
  void handleAccountNotFoundException() {

    var accountNotFoundException = new AccountNotFoundException("Account with id : Id-123 not found.");
    ResponseEntity<ErrorResponse> responseEntity =
        exceptionHandler.handleAccountNotFoundException(accountNotFoundException);

    Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
    Assertions.assertEquals("Account with id : Id-123 not found.", Objects.requireNonNull(
        responseEntity.getBody()).error());
    Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), responseEntity.getBody().code());
  }
}