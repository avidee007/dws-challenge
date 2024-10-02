package com.dws.challenge.web;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.aggregate.TransferResponse;
import com.dws.challenge.domain.command.TransferAmountCommand;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.service.AccountsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/v1/accounts")
@Slf4j
@RequiredArgsConstructor
public class AccountsController {

  private final AccountsService accountsService;


  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> createAccount(@RequestBody @Valid Account account) {
    log.info("Creating account {}", account);

    try {
      this.accountsService.createAccount(account);
    } catch (DuplicateAccountIdException daie) {
      return new ResponseEntity<>(daie.getMessage(), HttpStatus.BAD_REQUEST);
    }

    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @GetMapping(path = "/{accountId}")
  public Account getAccount(@PathVariable String accountId) {
    log.info("Retrieving account for id {}", accountId);
    return this.accountsService.getAccount(accountId);
  }


  /**
   * Api endpoint to initiate transfer between payer and receiver bank.
   * @param transferCommand holds accountId of payer and receiver back account.
   * @return ResponseEntity with success transfer status, which 200 OK status code.
   */
  @PostMapping("/transfer")
  public ResponseEntity<TransferResponse> transferAmount(@RequestBody @Valid TransferAmountCommand transferCommand){
    log.info("Initiating amount transfer.");
    log.debug("Initiating amount transfer, with payload : {}",transferCommand);
    return ResponseEntity.ok(accountsService.transferAmount(transferCommand));
  }

}