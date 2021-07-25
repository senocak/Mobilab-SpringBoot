package com.github.senocak.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.senocak.model.Transfer;
import com.github.senocak.util.TestConstants;
import com.github.senocak.exception.ServerException;
import com.github.senocak.payload.RequestSchema;
import com.github.senocak.payload.ResponseSchema;
import com.github.senocak.repository.TransferRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import static com.github.senocak.util.AppConstants.CURRENCY_TOKEN;

class TransferServiceTest {
    @InjectMocks TransferService transferService;

    @Mock TransferRepository transferRepository;
    @Mock ModelMapper modelMapper;
    @Mock UserService userService;
    @Mock RestTemplate restTemplate;
    @Mock ObjectMapper objectMapper;
    @Mock AccountService accountService;
    @Mock ResponseEntity responseEntity;

    @BeforeEach
    void init(){
        MockitoAnnotations.initMocks(this);
    }
    @Test
    void givenTransferWithAccountDoesNotBelongUserWhenValidateTransferObjectThenAssertResult() throws ServerException {
        // Given
        RequestSchema.Transfer transfer = new RequestSchema.Transfer();
        transfer.setCurrency(TestConstants.CURRENCY);
        transfer.setAmount(TestConstants.AMOUNT);
        transfer.setFromAccountId(UUID.fromString(TestConstants.ACCOUNT_1.getId()));
        transfer.setToAccountId(UUID.fromString(TestConstants.ACCOUNT_2.getId()));
        Mockito.doReturn(TestConstants.USER_1).when(userService).loggedInUser();
        Mockito.doReturn(TestConstants.ACCOUNT_2).when(accountService).findById(transfer.getFromAccountId().toString());
        // When
        Executable result = () -> transferService.validateTransferObject(transfer);
        // Then
        org.junit.jupiter.api.Assertions.assertThrows(ServerException.class, result);
    }
    @Test
    void givenTransferWithInvalidBalanceWhenValidateTransferObjectThenAssertResult() throws ServerException {
        // Given
        RequestSchema.Transfer transfer = new RequestSchema.Transfer();
        transfer.setCurrency(TestConstants.CURRENCY);
        transfer.setAmount(TestConstants.AMOUNT);
        TestConstants.ACCOUNT_1.setBalance(new BigDecimal(0));
        transfer.setFromAccountId(UUID.fromString(TestConstants.ACCOUNT_1.getId()));
        transfer.setToAccountId(UUID.fromString(TestConstants.ACCOUNT_2.getId()));
        Mockito.doReturn(TestConstants.USER_1).when(userService).loggedInUser();
        Mockito.doReturn(TestConstants.ACCOUNT_1).when(accountService).findById(transfer.getFromAccountId().toString());
        // When
        Executable result = () -> transferService.validateTransferObject(transfer);
        // Then
        org.junit.jupiter.api.Assertions.assertThrows(ServerException.class, result);
    }
    @Test
    void givenTransferWithInvalidBalanceConvertedWhenValidateTransferObjectThenAssertResult()
        throws ServerException, JsonProcessingException {
        // Given
        RequestSchema.Transfer transfer = new RequestSchema.Transfer();
        transfer.setCurrency(TestConstants.CURRENCY);
        transfer.setAmount(TestConstants.AMOUNT);
        transfer.setFromAccountId(UUID.fromString(TestConstants.ACCOUNT_1.getId()));
        transfer.setToAccountId(UUID.fromString(TestConstants.ACCOUNT_2.getId()));
        Mockito.doReturn(TestConstants.USER_1).when(userService).loggedInUser();
        Mockito.doReturn(TestConstants.ACCOUNT_1).when(accountService).findById(transfer.getFromAccountId().toString());
        Mockito.doReturn(responseEntity).when(restTemplate).getForEntity(CURRENCY_TOKEN + "?source="+transfer.getCurrency()+"&target="+TestConstants.ACCOUNT_1.getCurrency(), String.class);
        Mockito.doReturn("").when(responseEntity).getBody();
        ResponseSchema.CurrencyResponseFromAPI currencyResponseFromAPI = new ResponseSchema.CurrencyResponseFromAPI();
        Map<String, String> map = new HashMap<>();
        map.put("TRY", "2");
        currencyResponseFromAPI.setRates(map);
        Mockito.doReturn(currencyResponseFromAPI).when(objectMapper).readValue("", ResponseSchema.CurrencyResponseFromAPI.class);
        // When
        Executable result = () -> transferService.validateTransferObject(transfer);
        // Then
        org.junit.jupiter.api.Assertions.assertThrows(ServerException.class, result);
    }
    @Test
    void givenTransferWithInvalidCurrencyWhenValidateTransferObjectThenAssertResult()
        throws ServerException, JsonProcessingException {
        // Given
        RequestSchema.Transfer transfer = new RequestSchema.Transfer();
        transfer.setCurrency("invalid");
        transfer.setAmount(TestConstants.AMOUNT);
        transfer.setFromAccountId(UUID.fromString(TestConstants.ACCOUNT_1.getId()));
        transfer.setToAccountId(UUID.fromString(TestConstants.ACCOUNT_2.getId()));
        Mockito.doReturn(TestConstants.USER_1).when(userService).loggedInUser();
        Mockito.doReturn(TestConstants.ACCOUNT_1).when(accountService).findById(transfer.getFromAccountId().toString());
        Mockito.doReturn(responseEntity).when(restTemplate).getForEntity(CURRENCY_TOKEN + transfer.getCurrency(), String.class);
        Mockito.doReturn("").when(responseEntity).getBody();
        ResponseSchema.CurrencyResponseFromAPI currencyResponseFromAPI = new ResponseSchema.CurrencyResponseFromAPI();
        Map<String, String> map = new HashMap<>();
        map.put("TRY", "0");
        currencyResponseFromAPI.setRates(map);
        Mockito.doReturn(currencyResponseFromAPI).when(objectMapper).readValue("", ResponseSchema.CurrencyResponseFromAPI.class);
        Mockito.doReturn(TestConstants.ACCOUNT_2).when(accountService).findById(transfer.getToAccountId().toString());
        // When
        Executable result = () -> transferService.validateTransferObject(transfer);
        // Then
        org.junit.jupiter.api.Assertions.assertThrows(ServerException.class, result);
    }
    @Test
    void givenCurrencyAndAmountAndFromAccountIdAndToAccountIdWhenCreateThenAssertResult()
        throws ServerException, JsonProcessingException {
        // Given
        Mockito.doReturn(TestConstants.TRANSFER).when(transferRepository).save(Mockito.any(
            Transfer.class));
        Mockito.doReturn(TestConstants.ACCOUNT_1).when(accountService).findById(TestConstants.ACCOUNT_1.getId());
        Mockito.doReturn(TestConstants.ACCOUNT_1).when(accountService).findById(TestConstants.ACCOUNT_2.getId());
        Mockito.doReturn(TestConstants.ACCOUNT_1).when(accountService).findById(TestConstants.TRANSFER
            .getFromAccountId().getId());
        Mockito.doReturn(TestConstants.ACCOUNT_2).when(accountService).findById(TestConstants.TRANSFER
            .getToAccountId().getId());

        Mockito.doReturn(responseEntity).when(restTemplate).getForEntity(CURRENCY_TOKEN + "?source="+TestConstants.TRANSFER
            .getCurrency()+"&target="+TestConstants.ACCOUNT_1.getCurrency(), String.class);
        Mockito.doReturn(responseEntity).when(restTemplate).getForEntity(CURRENCY_TOKEN + "?source="+TestConstants.TRANSFER
            .getCurrency()+"&target="+TestConstants.ACCOUNT_2.getCurrency(), String.class);
        Mockito.doReturn("").when(responseEntity).getBody();

        ResponseSchema.CurrencyResponseFromAPI currencyResponseFromAPI = new ResponseSchema.CurrencyResponseFromAPI();
        Map<String, String> map = new HashMap<>();
        map.put("TRY", "2");
        currencyResponseFromAPI.setRates(map);
        Mockito.doReturn(currencyResponseFromAPI).when(objectMapper).readValue("", ResponseSchema.CurrencyResponseFromAPI.class);

        // When
        Transfer
            transfer = transferService.create(TestConstants.CURRENCY, TestConstants.AMOUNT, UUID.fromString(TestConstants.ACCOUNT_1.getId()), UUID.fromString(TestConstants.ACCOUNT_2.getId()));
        // Then
        Assertions.assertThat(transfer).isEqualTo(TestConstants.TRANSFER);
    }
    @Test
    void givenTransferTypeAndAccountIdAndPageableAndFromDateAndToDateWithInvalidUserAccountWhenGetAllForAccountThenAssertResult()
        throws ServerException {
        // Given
        Mockito.doReturn(TestConstants.USER_1).when(userService).loggedInUser();
        Mockito.doReturn(TestConstants.ACCOUNT_2).when(accountService).findById(TestConstants.ACCOUNT_1.getId());
        // When
        Executable result = () -> transferService.getAllForAccount("", TestConstants.ACCOUNT_1.getId(), Mockito.mock(Pageable.class), null, null);
        // Then
        org.junit.jupiter.api.Assertions.assertThrows(ServerException.class, result);
    }
    @Test
    void givenTransferTypeAndAccountIdAndPageableAndFromDateAndToDateWhenGetAllForAccountThenAssertResult()
        throws ServerException {
        // Given
        Instant from = Instant.now();
        Instant to = Instant.now();
        Pageable paging = PageRequest.of(1, 1);
        Mockito.doReturn(TestConstants.USER_1).when(userService).loggedInUser();
        Mockito.doReturn(TestConstants.ACCOUNT_1).when(accountService).findById(TestConstants.ACCOUNT_1.getId());
        Mockito.doReturn(Mockito.mock(Page.class)).when(transferRepository).findAllByToAccountIdAndCreatedAtIsBetween(TestConstants.ACCOUNT_1, from, to, paging);
        // When
        ResponseSchema.PagedTransferResponse getAllForAccount = transferService.getAllForAccount("incomings", TestConstants.ACCOUNT_1.getId(), paging, from, to);
        // Then
        Assertions.assertThat(getAllForAccount.getContent()).isEmpty();
        Assertions.assertThat(getAllForAccount.getPage()).isOne();
        Assertions.assertThat(getAllForAccount.getTotalElements()).isZero();
        Assertions.assertThat(getAllForAccount.getTotalPages()).isZero();
        Assertions.assertThat(getAllForAccount.isPrevious()).isFalse();
        Assertions.assertThat(getAllForAccount.isNext()).isFalse();
        Assertions.assertThat(getAllForAccount.getSort()).isNull();
    }
}
