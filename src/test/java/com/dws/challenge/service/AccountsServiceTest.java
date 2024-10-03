package com.dws.challenge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.command.TransferAmountCommand;
import com.dws.challenge.exception.AccountNotFoundException;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.exception.TransferAmountException;
import com.dws.challenge.repository.AccountsRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccountsServiceTest {

  @Mock
  private NotificationService notificationService;

  @Mock
  private AccountsRepository accountsRepository;

  @InjectMocks
  private AccountsService accountsService;

  @BeforeEach
  void setUp(){
    accountsService.getAccountsRepository().clearAccounts();
  }

  @Test
  void addAccount() {
    Account account = new Account("Id-123");
    account.setBalance(new BigDecimal(1000));
    Mockito.doNothing().when(accountsRepository).createAccount(account);
    Mockito.when(accountsRepository.getAccount(account.getAccountId())).thenReturn(account);

    this.accountsService.createAccount(account);

    assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(account);
  }

  @Test
  void addAccount_failsOnDuplicateId() {
    Account account = new Account("Id-123",BigDecimal.valueOf(100.50));


    Mockito.doThrow(new DuplicateAccountIdException("Account id Id-123 already exists!")).when(accountsRepository).createAccount(account);

    DuplicateAccountIdException exception =
        assertThrows(DuplicateAccountIdException.class, () -> accountsService.createAccount(account));
    assertEquals("Account id Id-123 already exists!",exception.getMessage());
  }
  

  /*
    Should do successful transfer without any deadlock
    when account ids are shorted lexicographically.
    Consistent locking ensures no deadlock.
   */
  @Test
  void transferAmount_success_when_accountIds_shorted_lexicographically() {
    Account payer = new Account("Id-123", BigDecimal.valueOf(200.50));
    Account payee = new Account("Id-456", BigDecimal.valueOf(100.50));

    this.accountsService.createAccount(payer);
    this.accountsService.createAccount(payee);

    Mockito.when(accountsRepository.getAccount(payer.getAccountId())).thenReturn(payer);
    Mockito.when(accountsRepository.getAccount(payee.getAccountId())).thenReturn(payee);
    Mockito.doNothing().when(notificationService).notifyAboutTransfer(any(Account.class), anyString());

    TransferAmountCommand transferCommand =
        new TransferAmountCommand("Id-123", "Id-456", BigDecimal.valueOf(50.00));
    this.accountsService.transferAmount(transferCommand);

    Assertions.assertEquals(BigDecimal.valueOf(150.50), payer.getBalance());
    Assertions.assertEquals(BigDecimal.valueOf(150.50), payee.getBalance());
    Mockito.verify(notificationService, Mockito.times(2)).notifyAboutTransfer(any(), anyString());
  }

  /*
    Should do successful transfer without any deadlock
    when account ids are not shorted lexicographically.
    Consistent locking ensures no deadlock.
   */
  @Test
  void transferAmount_success_when_accountIds_not_shorted_lexicographically() {
    Account payer = new Account("Id-456", BigDecimal.valueOf(200.50));
    Account payee = new Account("Id-123", BigDecimal.valueOf(100.50));

    this.accountsService.createAccount(payer);
    this.accountsService.createAccount(payee);

    Mockito.when(accountsRepository.getAccount(payer.getAccountId())).thenReturn(payer);
    Mockito.when(accountsRepository.getAccount(payee.getAccountId())).thenReturn(payee);
    Mockito.doNothing().when(notificationService).notifyAboutTransfer(any(Account.class), anyString());

    TransferAmountCommand transferCommand =
        new TransferAmountCommand("Id-456", "Id-123", BigDecimal.valueOf(50.00));
    this.accountsService.transferAmount(transferCommand);

    Assertions.assertEquals(BigDecimal.valueOf(150.50), payer.getBalance());
    Assertions.assertEquals(BigDecimal.valueOf(150.50), payee.getBalance());
    Mockito.verify(notificationService, Mockito.times(2)).notifyAboutTransfer(any(), anyString());
  }

  /*
    Should throw exception if any of the accountId does not exist or no account found with given
    accountId.
  */
  @Test
  void transferAmount_should_throw_AccountNotFoundException_if_account_does_not_exists() {

    Mockito.when(accountsRepository.getAccount("Id-123")).thenReturn(null);

    var transferCommand = new TransferAmountCommand("Id-123", "Id-456", BigDecimal.valueOf(50.00));
    AccountNotFoundException accountNotFoundException = assertThrows(AccountNotFoundException.class,
        () -> accountsService.transferAmount(transferCommand));

    assertEquals("Account with id: Id-123 not found.", accountNotFoundException.getMessage());
    Mockito.verifyNoInteractions(notificationService);
  }

  /*
     Should throw exception if account balance is less than the transfer amount to avoid overdraft.
  */
  @Test
  void transferAmount_should_throw_TransferAmountException_if_payer_balance_is_less() {
    Account payer = new Account("Id-123", BigDecimal.valueOf(20.50));
    Account payee = new Account("Id-456", BigDecimal.valueOf(100.50));

    this.accountsService.createAccount(payer);
    this.accountsService.createAccount(payee);

    Mockito.when(accountsRepository.getAccount(payer.getAccountId())).thenReturn(payer);
    Mockito.when(accountsRepository.getAccount(payee.getAccountId())).thenReturn(payee);

    var transferCommand = new TransferAmountCommand("Id-123", "Id-456", BigDecimal.valueOf(50.00));
    TransferAmountException transferAmountException = assertThrows(TransferAmountException.class,
        () -> accountsService.transferAmount(transferCommand));

    assertEquals("Insufficient funds in the account in account number : Id-123.",
        transferAmountException.getMessage());
    Mockito.verifyNoInteractions(notificationService);
  }


}