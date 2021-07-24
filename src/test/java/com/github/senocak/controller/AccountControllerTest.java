package com.github.senocak.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.senocak.service.AccountService;
import com.github.senocak.util.AppConstants;
import com.github.senocak.util.TestConstants;
import com.github.senocak.exception.ServerException;
import com.github.senocak.payload.RequestSchema;
import com.github.senocak.payload.ResponseSchema;
import com.github.senocak.service.TransferService;
import com.github.senocak.service.UserService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

class AccountControllerTest {
    @InjectMocks AccountController accountController;

    @Mock UserService userService;
    @Mock AccountService accountService;
    @Mock ModelMapper modelMapper;
    @Mock TransferService transferService;
    private final ResponseSchema response = new ResponseSchema(true, null);

    @BeforeEach
    void init(){
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void givenNoneWhenGetMeThenAssertResult() throws ServerException {
        // Given
        Mockito.doReturn(TestConstants.USER_1).when(userService).loggedInUser();
        Mockito.doReturn(TestConstants.USER_PROFILE).when(modelMapper).map(TestConstants.USER_1, ResponseSchema.UserProfile.class);
        response.setMessage(TestConstants.USER_PROFILE);
        // When
        ResponseEntity<?> responseEntity = accountController.getMe();
        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(responseEntity.getBody()).isEqualToComparingFieldByField(response);
    }

    @Test
    void givenUserUpdateProfileObjectWhenPatchMeThenAssertResult() throws ServerException {
        // Given
        RequestSchema.UserUpdateProfile userUpdateProfile = new RequestSchema.UserUpdateProfile();
        userUpdateProfile.setUsername(TestConstants.NAME);
        userUpdateProfile.setEmail(TestConstants.EMAIL);
        userUpdateProfile.setName(TestConstants.NAME);
        Mockito.doReturn(TestConstants.USER_1).when(userService).patchUser(userUpdateProfile);
        Mockito.doReturn(TestConstants.USER_PROFILE).when(modelMapper).map(TestConstants.USER_1, ResponseSchema.UserProfile.class);
        response.setMessage(TestConstants.USER_PROFILE);
        // When
        ResponseEntity<?> responseEntity = accountController.patchMe(userUpdateProfile);
        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(responseEntity.getBody()).isEqualToComparingFieldByField(response);
    }

    @Test
    void givenAccountIdWhenDeleteAccountThenAssertResult() throws ServerException {
        // Given
        Mockito.doNothing().when(accountService).deleteAccountById(TestConstants.ACCOUNT_1.getId());
        response.setMessage(new String[]{"Account Deleted"});
        // When
        ResponseEntity<?> responseEntity = accountController.deleteAccount(TestConstants.ACCOUNT_1.getId());
        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(responseEntity.getBody()).isEqualToComparingFieldByField(response);
    }

    @Test
    void givenNewAccountObjectWhenAddAccountThenAssertResult() throws ServerException {
        // Given
        RequestSchema.NewAccount newAccount = new RequestSchema.NewAccount();
        newAccount.setName(TestConstants.NAME);
        newAccount.setCurrency(AppConstants.CurrencyEnum.USD.getCurrency());

        Mockito.doNothing().when(accountService).validateCurrency(AppConstants.CurrencyEnum.USD.getCurrency());
        Mockito.doReturn(TestConstants.ACCOUNT_1).when(accountService).addNewAccount(newAccount);
        ResponseSchema.AccountResponse accountResponse = new ResponseSchema.AccountResponse();
        Mockito.doReturn(accountResponse).when(modelMapper).map(TestConstants.ACCOUNT_1, ResponseSchema.AccountResponse.class);
        response.setMessage(accountResponse);
        // When
        ResponseEntity<?> responseEntity = accountController.addAccount(newAccount);
        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Assertions.assertThat(responseEntity.getBody()).isEqualToComparingFieldByField(response);
    }

    @Test
    void givenTransferObjectWhenPostTransferThenAssertResult()
        throws ServerException, JsonProcessingException {
        // Given
        RequestSchema.Transfer transfer = new RequestSchema.Transfer();
        transfer.setCurrency(TestConstants.CURRENCY);
        transfer.setAmount(TestConstants.AMOUNT);
        transfer.setFromAccountId(UUID.fromString(TestConstants.ACCOUNT_1.getId()));
        transfer.setToAccountId(UUID.fromString(TestConstants.ACCOUNT_2.getId()));
        Mockito.doReturn(TestConstants.TRANSFER).when(transferService).create(transfer.getCurrency(), transfer.getAmount(), transfer.getFromAccountId(), transfer.getToAccountId());
        Mockito.doReturn(TestConstants.TRANSFER).when(modelMapper).map(TestConstants.TRANSFER, ResponseSchema.TransferResponse.class);
        response.setMessage(TestConstants.TRANSFER);
        // When
        ResponseEntity<?> responseEntity = accountController.postTransfer(transfer);
        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Assertions.assertThat(responseEntity.getBody()).isEqualToComparingFieldByField(response);
    }

    @Test
    void givenNotValidTransferTypeWhenGetIncomingsForAccountThenAssertResult() {
        // When
        Executable result = () -> accountController.getTransfersForAccount(TestConstants.ACCOUNT_1.getId(), "notValidincomings", 1, 10, "asc", "", "");
        // Then
        org.junit.jupiter.api.Assertions.assertThrows(ServerException.class, result);
    }

    @Test
    void givenNotValidPageWhenGetIncomingsForAccountThenAssertResult() {
        // When
        Executable result = () -> accountController.getTransfersForAccount(TestConstants.ACCOUNT_1.getId(), "incomings", 0, 10, "asc", "", "");
        // Then
        org.junit.jupiter.api.Assertions.assertThrows(ServerException.class, result);
    }

    @Test
    void givenNotValidSizeWhenGetIncomingsForAccountThenAssertResult() {
        // When
        Executable result = () -> accountController.getTransfersForAccount(TestConstants.ACCOUNT_1.getId(), "incomings", 1, 0, "asc", "", "");
        // Then
        org.junit.jupiter.api.Assertions.assertThrows(ServerException.class, result);
    }

    @Test
    void givenNotValidOrderWhenGetIncomingsForAccountThenAssertResult() {
        // When
        Executable result = () -> accountController.getTransfersForAccount(TestConstants.ACCOUNT_1.getId(), "incomings", 1, 10, "NotValid", "", "");
        // Then
        org.junit.jupiter.api.Assertions.assertThrows(ServerException.class, result);
    }

    @Test
    void givenValidDatasWhenGetIncomingsForAccountThenAssertResult() throws ServerException {
        // Given
        String transferType = "incomings";
        String accountId = TestConstants.ACCOUNT_1.getId();

        Mockito.doReturn(TestConstants.PAGED_TRANSFER_RESPONSE).when(transferService).getAllForAccount(
            Mockito.eq(transferType),
            Mockito.eq(transferType),
            Mockito.any(Pageable.class),
            Mockito.eq(null),
            Mockito.eq(null));
        Mockito.doReturn(TestConstants.PAGED_TRANSFER_RESPONSE).when(modelMapper).map(null, ResponseSchema.PagedTransferResponse.class);
        response.setMessage(TestConstants.PAGED_TRANSFER_RESPONSE);
        // When
        ResponseEntity<?> responseEntity = accountController.getTransfersForAccount(accountId, transferType, 1, 10, "asc", "", "");
        // Then
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(responseEntity.getBody()).isEqualToComparingFieldByField(response);
    }
}
