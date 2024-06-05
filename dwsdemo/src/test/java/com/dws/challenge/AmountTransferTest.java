package com.dws.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import com.dws.challenge.domain.Account;
import com.dws.challenge.service.AccountsService;
import com.fasterxml.jackson.databind.ObjectMapper;
/**
 * Test class for transfer amount
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@WebAppConfiguration
class AmountTransferTest {

  private MockMvc mockMvc;

  @Autowired
  private AccountsService accountsService;

  @Autowired
  private WebApplicationContext webApplicationContext;

  /**
   * Initialize the account details
   * @throws Exception
   */
  @BeforeEach
  void prepareMockMvc() throws Exception {
    this.mockMvc = webAppContextSetup(this.webApplicationContext).build();

    accountsService.getAccountsRepository().clearAccounts();
    
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
		      .content("{\"accountId\":\"Id-100\",\"balance\":1000}")).andExpect(status().isCreated());

	this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
		      .content("{\"accountId\":\"Id-101\",\"balance\":2000}")).andExpect(status().isCreated());
	  
  }

  
  /**
   * Testing the success scenario
   * @throws Exception
   */
  @Test
  void transferAccountSuccess() throws Exception {
    this.mockMvc.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountFromId\":\"Id-100\",\"accountToId\":\"Id-101\",\"amountToTransfer\":500 }")).andExpect(status().isOk());

    Account fromAccount = accountsService.getAccount("Id-100");
    assertThat(fromAccount.getBalance()).isEqualByComparingTo("500");
    
    Account toAccount = accountsService.getAccount("Id-101");
    assertThat(toAccount.getBalance()).isEqualByComparingTo("2500");
  }

  /**
   * Testing low balance validation
   * @throws Exception
   */
  @Test
  void transferAccountBalanceLow() throws Exception {
	  MvcResult mvcResult =  this.mockMvc.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountFromId\":\"Id-100\",\"accountToId\":\"Id-101\",\"amountToTransfer\":3000 }")).andReturn();
	  assertThat(mvcResult.getResponse().getContentAsString()).contains("ACCOUNT_BALANCE_LOW");
  }

  /**
   * Testing from account not available validation
   * @throws Exception
   */
  @Test
  void fromAccountNotAvailable() throws Exception {
	  MvcResult mvcResult =  this.mockMvc.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountFromId\":\"Id-10033\",\"accountToId\":\"Id-101\",\"amountToTransfer\":3000 }")).andReturn();
	  assertThat(mvcResult.getResponse().getContentAsString()).contains("FROM_ACCOUNT_NOT_AVAILABLE");
  }
  
  /**
   * Testing to account not available validation
   * @throws Exception
   */
  @Test
  void toAccountNotAvailable() throws Exception {
	  MvcResult mvcResult =  this.mockMvc.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountFromId\":\"Id-100\",\"accountToId\":\"Id-10155\",\"amountToTransfer\":3000 }")).andReturn();
	  assertThat(mvcResult.getResponse().getContentAsString()).contains("TO_ACCOUNT_NOT_AVAILABLE");
  }
  
  /**
   * Negative balance transfer validation
   * @throws Exception
   */
  @Test
  void negativeTransfer() throws Exception {
	  MvcResult mvcResult =  this.mockMvc.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountFromId\":\"Id-100\",\"accountToId\":\"Id-10155\",\"amountToTransfer\":-22 }")).andReturn();
	  assertThat(mvcResult.getResponse().getContentAsString()).contains("TRANSFER_AMOUNT_IN_NEGATIVE");
  }
  
}
