package com.dws.challenge.service;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.aggregate.TransferResponse;
import com.dws.challenge.domain.command.TransferAmountCommand;
import com.dws.challenge.domain.valueobject.TransferStatus;
import com.dws.challenge.exception.AccountNotFoundException;
import com.dws.challenge.exception.TransferAmountException;
import com.dws.challenge.repository.AccountsRepository;
import java.math.BigDecimal;
import java.util.Objects;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@Getter
public class AccountsService {

  private final AccountsRepository accountsRepository;
  private final NotificationService notificationService;

  public void createAccount(Account account) {
    this.accountsRepository.createAccount(account);
  }

  public Account getAccount(String accountId) {
    return this.accountsRepository.getAccount(accountId);
  }

  /**
   * Performs transfer of given amount from payer account to payee account.
   * @param command contains accountId of payer and payee bank accounts.
   * @return Transfer response DTO if transfer was successful.
   */
  public TransferResponse transferAmount(TransferAmountCommand command) {

    BigDecimal amount = command.amount();
    String payerAccountId = command.accountFromId();
    String payeeAccountId = command.accountToId();
    String firstLockAccNum = payerAccountId.compareTo(payeeAccountId) < 0 ? payerAccountId : payeeAccountId;
    String secondLockAccNum = payerAccountId.compareTo(payeeAccountId) < 0 ? payeeAccountId : payerAccountId;

    synchronized (getLockObject(firstLockAccNum)) {
      synchronized (getLockObject(secondLockAccNum)) {

        Account payer = validateAccount(getAccount(payerAccountId),payerAccountId);
        Account payee = validateAccount(getAccount(payeeAccountId),payeeAccountId);

        if (payer.getBalance().compareTo(amount) < 0) {
          String message = String.format("Insufficient funds in the account in account number : %s.",
              payer.getAccountId());
          log.error(message);
          throw new TransferAmountException(message);
        }

        payer.setBalance(payer.getBalance().subtract(amount));
        payee.setBalance(payee.getBalance().add(amount));

        sendTransferNotification(payer,payee);
      }
    }
      log.info("Amount transfer from {} to {} of amount: {} was successful.",
          command.accountFromId(),command.accountToId(),command.amount());
      return new TransferResponse(TransferStatus.SUCCESS, command.amount());
  }

  private Object getLockObject(String accountNumber) {
    return accountNumber.intern();
  }

  private Account validateAccount(Account account,String accountId) {
    if (Objects.isNull(account)) {
      var message = String.format("Account with id: %s not found.",accountId);
      log.error(message);
      throw new AccountNotFoundException(message);
    }
    return account;
  }

  private void sendTransferNotification(Account accountFrom, Account accountTo) {
    String payerDescription = "Successfully transferred account to: "+ accountFrom.getAccountId();
    String payeeDescription = "Amount received from account: "+ accountFrom.getAccountId();
    notificationService.notifyAboutTransfer(accountFrom, payerDescription);
    notificationService.notifyAboutTransfer(accountTo, payeeDescription);
  }
}