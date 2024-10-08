package com.dws.challenge.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import com.dws.challenge.domain.Account;
import com.dws.challenge.service.AccountsService;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@WebAppConfiguration
class AccountsControllerTest {

  private MockMvc mockMvc;

  @Autowired
  private AccountsService accountsService;

  @Autowired
  private WebApplicationContext webApplicationContext;

  @BeforeEach
  void prepareMockMvc() {
    this.mockMvc = webAppContextSetup(this.webApplicationContext).build();

    // Reset the existing accounts before each test.
    accountsService.getAccountsRepository().clearAccounts();
  }

  @Test
  void createAccount() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

    Account account = accountsService.getAccount("Id-123");
    assertThat(account.getAccountId()).isEqualTo("Id-123");
    assertThat(account.getBalance()).isEqualByComparingTo("1000");
  }

  @Test
  void createDuplicateAccount() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  void createAccountNoAccountId() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  void createAccountNoBalance() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\"}")).andExpect(status().isBadRequest());
  }

  @Test
  void createAccountNoBody() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isBadRequest());
  }

  @Test
  void createAccountNegativeBalance() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":-1000}")).andExpect(status().isBadRequest());
  }

  @Test
  void createAccountEmptyAccountId() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"\",\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  void getAccount() throws Exception {
    String uniqueAccountId = "Id-" + System.currentTimeMillis();
    Account account = new Account(uniqueAccountId, new BigDecimal("123.45"));
    this.accountsService.createAccount(account);
    this.mockMvc.perform(get("/v1/accounts/" + uniqueAccountId))
      .andExpect(status().isOk())
      .andExpect(
        content().string("{\"accountId\":\"" + uniqueAccountId + "\",\"balance\":123.45}"));
  }

  //Expect 400: BAD Request, when either payer or payee accountId is null.
  @Test
  void test_transferAmount_when_any_accountId_is_null() throws Exception {

    var requestBodyWithFromAccountIdNull= """
        {
          "accountToId" : "Id-456",
          "amount": 50.00
        }""";

    this.mockMvc.perform(
            post("/v1/accounts/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBodyWithFromAccountIdNull)
        )
        .andExpect(status().isBadRequest());
  }

  //Expect 400: BAD Request, when the amount being transfer is 0.
  @Test
  void test_transferAmount_when_transfer_account_is_zero() throws Exception {

    var requestBodyWithZeroTransferAmount= """
        {
          "accountFromId":"Id-123",
          "accountToId" : "Id-456",
          "amount": 0
        }""";

    this.mockMvc.perform(
            post("/v1/accounts/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBodyWithZeroTransferAmount)
        )
        .andExpect(status().isBadRequest());
  }

  //Expect 400: BAD Request, when the amount being transfer is negative.
  @Test
  void test_transferAmount_when_transfer_account_is_negative() throws Exception {

    var requestBodyWithNegativeTransferAmount= """
        {
          "accountFromId":"Id-123",
          "accountToId" : "Id-456",
          "amount": -50.00
        }""";

    this.mockMvc.perform(
            post("/v1/accounts/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBodyWithNegativeTransferAmount)
        )
        .andExpect(status().isBadRequest());
  }

  //Expect 400: BAD Request, when both accountFromId and accountToId are same.
  @Test
  void test_transferAmount_when_both_accountIds_are_same() throws Exception {

    var requestBodyWithSameAccountIds= """
        {
          "accountFromId":"Id-123",
          "accountToId" : "Id-123",
          "amount": 50.00
        }""";

    this.mockMvc.perform(
            post("/v1/accounts/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBodyWithSameAccountIds)
        )
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.status").value("Failure"))
        .andExpect(
            jsonPath("$.code").value(400))
        .andExpect(
            jsonPath("$.error").value("Transfer between same account is not allowed."));
  }

  //Expect 500: Internal Server Error, when transfer amount is bigger than payer's account balance.
  @Test
  void test_transferAmount_when_transfer_amount_is_bigger_than_payers_balance() throws Exception {

    Account payer = new Account("Id-123", BigDecimal.valueOf(100.50));
    Account payee = new Account("Id-456", BigDecimal.valueOf(10.50));

    this.accountsService.createAccount(payer);
    this.accountsService.createAccount(payee);

    var requestBodyWithBiggerTransferMoneyThanBalance= """
        {
          "accountFromId":"Id-123",
          "accountToId" : "Id-456",
          "amount": 500.00
        }""";


    this.mockMvc.perform(
            post("/v1/accounts/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBodyWithBiggerTransferMoneyThanBalance)
        )
        .andExpect(status().isInternalServerError())
        .andExpect(
            jsonPath("$.status").value("Failure"))
        .andExpect(
            jsonPath("$.code").value(500))
        .andExpect(
            jsonPath("$.error").value("Insufficient fund balance in account number : Id-123.")
        );
  }

  //Expect 200: OK, when request with valid request body.
  @Test
  void test_transferAmount_success() throws Exception {

    Account payer = new Account("Id-123", BigDecimal.valueOf(200.50));
    Account payee = new Account("Id-456", BigDecimal.valueOf(100.50));

    this.accountsService.createAccount(payer);
    this.accountsService.createAccount(payee);

    var validRequestBody= """
        {
          "accountFromId":"Id-123",
          "accountToId" : "Id-456",
          "amount": 50.00
        }""";

    var expectedResponseBody = """
		{
		"status":"SUCCESS",
		"transferredAmount": 50.00
	}""";

    this.mockMvc.perform(
        post("/v1/accounts/transfer")
            .contentType(MediaType.APPLICATION_JSON)
            .content(validRequestBody)
        )
        .andExpect(status().isOk())
        .andExpect(content().json(expectedResponseBody));
  }
}