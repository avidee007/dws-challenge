package com.dws.challenge.repository;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.DuplicateAccountIdException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccountsRepositoryInMemoryTest {

  @InjectMocks
  private AccountsRepositoryInMemory repositoryInMemory;

  @BeforeEach
  void setUp() {
    repositoryInMemory.clearAccounts();
  }

  @Test
  void createAccount_with_unique_account_id() {
    Account account = new Account("Id-123", BigDecimal.valueOf(200.50));
    Assertions.assertDoesNotThrow(() -> repositoryInMemory.createAccount(account));
  }

  @Test
  void createAccount_with_duplicate_account_id() {
    Account account = new Account("Id-123", BigDecimal.valueOf(200.50));
    Account duplicate = new Account("Id-123", BigDecimal.valueOf(100.50));
    repositoryInMemory.createAccount(account);

    Assertions.assertThrows(DuplicateAccountIdException.class,
        () -> repositoryInMemory.createAccount(duplicate));
  }

  @Test
  void getAccount() {
    Account account = new Account("Id-123", BigDecimal.valueOf(200.50));
    repositoryInMemory.createAccount(account);

    Account resultAccount = repositoryInMemory.getAccount("Id-123");

    Assertions.assertEquals(account.getAccountId(), resultAccount.getAccountId());
    Assertions.assertEquals(account.getBalance(), resultAccount.getBalance());

  }
}