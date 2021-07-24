package com.github.senocak.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.senocak.integration.annotations.SpringBootTestConfig;
import com.github.senocak.payload.RequestSchema;
import com.github.senocak.util.AppConstants;
import com.github.senocak.util.TestConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@SpringBootTestConfig
class AccountControllerIT {
    @Autowired WebApplicationContext webApplicationContext;
    @Autowired MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private HttpHeaders httpHeaders;
    private static final String AUTHORIZATION = "Authorization";
    private static final String BASE_URL = "/api/v1/user";
    private static final String AUTH_LOGIN_URL = "/api/v1/auth/login";
    private static final String FROM_ACCOUNT_ID1 = "636c1e45-eac1-4c42-b4c5-ffb6883dee12";
    private static final String FROM_ACCOUNT_ID2 = "d5dc1164-8ef7-474d-8e53-665390dde1c7";
    private static final String TO_ACCOUNT_ID = "bcd674d3-9a95-453e-8f11-9f341f9c7d47";
    // These uuid's are coming from db.sql

    @BeforeEach public void init() throws Exception {
        httpHeaders = new HttpHeaders();
        objectMapper = new ObjectMapper();
        // To Get Proper JWT Token
        RequestSchema.LoginRequest loginRequest = new RequestSchema.LoginRequest();
        loginRequest.setUsernameOrEmail(TestConstants.EMAIL);
        loginRequest.setPassword(TestConstants.PASS);
        RequestBuilder requestBuilder = MockMvcRequestBuilders
            .post(AUTH_LOGIN_URL)
            .content(objectMapper.writeValueAsString(loginRequest))
            .contentType(MediaType.APPLICATION_JSON);
        // When
        MvcResult result = mockMvc.perform(requestBuilder).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
        String content = result.getResponse().getContentAsString();
        Map message = objectMapper.readValue(content, Map.class);
        Map token = objectMapper.readValue(objectMapper.writeValueAsString(message.get("message")), Map.class);
        httpHeaders.add(AUTHORIZATION, token.get("token").toString());
    }
    @Test
    void givenNoneWhenMeThenAssertResult() throws Exception {
        // Given
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get(BASE_URL+"/me").headers(httpHeaders);
        // When
        mockMvc.perform(requestBuilder)
            // Then
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message.name").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.message.email").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.message.username").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.message.accounts").exists());
    }
    @Test
    void givenTransferObjectWhenPostTransferThenAssertResult() throws Exception {
        // Given
        RequestSchema.Transfer transfer = new RequestSchema.Transfer();
        transfer.setCurrency(TestConstants.CURRENCY);
        transfer.setAmount(TestConstants.AMOUNT);
        transfer.setFromAccountId(UUID.fromString(FROM_ACCOUNT_ID1));
        transfer.setToAccountId(UUID.fromString(TO_ACCOUNT_ID));
        RequestBuilder requestBuilder = MockMvcRequestBuilders
            .post(BASE_URL+"/send")
            .content(objectMapper.writeValueAsString(transfer))
            .contentType(MediaType.APPLICATION_JSON)
            .headers(httpHeaders);
        // When
        mockMvc.perform(requestBuilder)
            // Then
            .andExpect(MockMvcResultMatchers.status().isCreated())
            .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message.currency").value(TestConstants.CURRENCY))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message.fromAccountId.id").value(
                FROM_ACCOUNT_ID1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message.toAccountId.id").value(TO_ACCOUNT_ID))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message.createdAt").exists());
    }
    @Test
    void givenNoneWithInvalidTransferTypeObjectWhenGetTransfersForAccountThenAssertResult() throws Exception {
        // Given
        RequestSchema.Transfer transfer = new RequestSchema.Transfer();
        transfer.setCurrency(TestConstants.CURRENCY);
        transfer.setAmount(TestConstants.AMOUNT);
        transfer.setFromAccountId(UUID.fromString(FROM_ACCOUNT_ID1));
        transfer.setToAccountId(UUID.fromString(TO_ACCOUNT_ID));
        RequestBuilder requestBuilder = MockMvcRequestBuilders
            .get(BASE_URL + "/" + TO_ACCOUNT_ID + "/invalid")
            .headers(httpHeaders);
        // When
        mockMvc.perform(requestBuilder)
            // Then
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message[0]").value("SVC0001"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message[1]").value("Invalid input value for message part %1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message[2]").value("Transfer Type:invalid"));
    }
    @Test
    void givenNoneWithInvalidPageWhenGetTransfersForAccountThenAssertResult() throws Exception {
        // Given
        RequestSchema.Transfer transfer = new RequestSchema.Transfer();
        transfer.setCurrency(TestConstants.CURRENCY);
        transfer.setAmount(TestConstants.AMOUNT);
        transfer.setFromAccountId(UUID.fromString(FROM_ACCOUNT_ID1));
        transfer.setToAccountId(UUID.fromString(TO_ACCOUNT_ID));
        RequestBuilder requestBuilder = MockMvcRequestBuilders
            .get(BASE_URL + "/" + TO_ACCOUNT_ID + "/incomings?page=0")
            .headers(httpHeaders);
        // When
        mockMvc.perform(requestBuilder)
            // Then
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message[0]").value("SVC0001"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message[1]").value("Invalid input value for message part %1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message[2]").value("Page: 0"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message[3]").value("Size: 10"));
    }
    @Test
    void givenNoneWithInvalidSizeWhenGetTransfersForAccountThenAssertResult() throws Exception {
        // Given
        RequestSchema.Transfer transfer = new RequestSchema.Transfer();
        transfer.setCurrency(TestConstants.CURRENCY);
        transfer.setAmount(TestConstants.AMOUNT);
        transfer.setFromAccountId(UUID.fromString(FROM_ACCOUNT_ID1));
        transfer.setToAccountId(UUID.fromString(TO_ACCOUNT_ID));
        RequestBuilder requestBuilder = MockMvcRequestBuilders
            .get(BASE_URL + "/" + TO_ACCOUNT_ID + "/incomings?size=0")
            .headers(httpHeaders);
        // When
        mockMvc.perform(requestBuilder)
            // Then
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message[0]").value("SVC0001"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message[1]").value("Invalid input value for message part %1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message[2]").value("Page: 1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message[3]").value("Size: 0"));
    }
    @Test
    void givenNoneWithInvalidOrderByWhenGetTransfersForAccountThenAssertResult() throws Exception {
        // Given
        RequestSchema.Transfer transfer = new RequestSchema.Transfer();
        transfer.setCurrency(TestConstants.CURRENCY);
        transfer.setAmount(TestConstants.AMOUNT);
        transfer.setFromAccountId(UUID.fromString(FROM_ACCOUNT_ID1));
        transfer.setToAccountId(UUID.fromString(TO_ACCOUNT_ID));
        RequestBuilder requestBuilder = MockMvcRequestBuilders
            .get(BASE_URL + "/" + TO_ACCOUNT_ID + "/incomings?by=invalid")
            .headers(httpHeaders);
        // When
        mockMvc.perform(requestBuilder)
            // Then
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message[0]").value("SVC0001"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message[1]").value("Invalid input value for message part %1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message[2]").value("Order: invalid"));
    }
    @Test
    void givenNoneWhenGetTransfersForAccountThenAssertResult() throws Exception {
        // Given
        RequestSchema.Transfer transfer = new RequestSchema.Transfer();
        transfer.setCurrency(TestConstants.CURRENCY);
        transfer.setAmount(TestConstants.AMOUNT);
        transfer.setFromAccountId(UUID.fromString(FROM_ACCOUNT_ID1));
        transfer.setToAccountId(UUID.fromString(TO_ACCOUNT_ID));
        RequestBuilder requestBuilder = MockMvcRequestBuilders
            .get(BASE_URL + "/" + FROM_ACCOUNT_ID1 + "/incomings")
            .headers(httpHeaders);
        // When
        mockMvc.perform(requestBuilder)
            // Then
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message.content").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.message.page").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message.totalElements").value(2))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message.totalPages").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message.previous").value(false))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message.next").value(false));
    }
    @Test
    void givenUserUpdateProfileWhenPatchMeThenAssertResult() throws Exception {
        // Given
        RequestSchema.UserUpdateProfile userUpdateProfile = new RequestSchema.UserUpdateProfile();
        userUpdateProfile.setName(TestConstants.NAME);
        userUpdateProfile.setEmail(TestConstants.EMAIL);
        userUpdateProfile.setUsername(TestConstants.NAME);
        RequestBuilder requestBuilder = MockMvcRequestBuilders
            .patch(BASE_URL+"/update")
            .content(objectMapper.writeValueAsString(userUpdateProfile))
            .contentType(MediaType.APPLICATION_JSON)
            .headers(httpHeaders);
        // When
        mockMvc.perform(requestBuilder)
            // Then
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message.name").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.message.email").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.message.username").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.message.accounts").exists());
    }
    @Test
    void givenAccountIdWhenDeleteAccountThenAssertResult() throws Exception {
        // Given
        RequestBuilder requestBuilder = MockMvcRequestBuilders
            .delete(BASE_URL + "/" + FROM_ACCOUNT_ID2)
            .headers(httpHeaders);
        // When
        mockMvc.perform(requestBuilder)
            // Then
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message[0]").value("Account Deleted"));
    }
    @Test
    void givenNewAccountObjectWhenAddAccountThenAssertResult() throws Exception {
        // Given
        RequestSchema.NewAccount newAccount = new RequestSchema.NewAccount();
        newAccount.setCurrency(AppConstants.CurrencyEnum.EUR.getCurrency());
        newAccount.setName(TestConstants.NAME);
        RequestBuilder requestBuilder = MockMvcRequestBuilders
            .post(BASE_URL + "/add")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(newAccount))
            .headers(httpHeaders);
        // When
        mockMvc.perform(requestBuilder)
            // Then
            .andExpect(MockMvcResultMatchers.status().isCreated())
            .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message.id").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.message.name").value(TestConstants.NAME))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message.currency").value(AppConstants.CurrencyEnum.EUR.getCurrency()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message.balance").value(new BigDecimal(0)));
    }
}
