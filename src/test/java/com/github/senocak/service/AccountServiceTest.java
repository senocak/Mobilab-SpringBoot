package com.github.senocak.service;

import com.github.senocak.exception.ServerException;
import com.github.senocak.model.Account;
import com.github.senocak.payload.RequestSchema;
import com.github.senocak.repository.AccountRepository;
import com.github.senocak.util.AppConstants;
import com.github.senocak.util.TestConstants;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import java.util.Optional;

class AccountServiceTest {
    @InjectMocks AccountService accountService;
    @Mock AccountRepository accountRepository;
    @Mock UserService userService;
    private static final String ID = "id";

    @BeforeEach
    void init(){
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void givenIdWhenFindByIdThenAssertResult() throws ServerException {
        // Given
        Mockito.doReturn(Optional.of(TestConstants.ACCOUNT_1)).when(accountRepository).findById(ID);
        // When
        Account account = accountService.findById(ID);
        // Then
        Assertions.assertThat(account).isEqualTo(TestConstants.ACCOUNT_1);
    }
    @Test
    void givenIdWithExceptionWhenFindByIdThenAssertResult() {
        // When
        Executable result = () -> accountService.findById(ID);
        // Then
        org.junit.jupiter.api.Assertions.assertThrows(ServerException.class, result);
    }
    @Test
    void givenAccountWhenSaveAccountThenAssertResult() {
        // Given
        Mockito.doReturn(TestConstants.ACCOUNT_1).when(accountRepository).save(TestConstants.ACCOUNT_1);
        // When
        Account account = accountService.saveAccount(TestConstants.ACCOUNT_1);
        // Then
        Assertions.assertThat(account).isEqualTo(TestConstants.ACCOUNT_1);
    }
    @Test
    void givenIdWithExceptionWhenDeleteAccountByIdThenAssertResult() throws ServerException {
        // Given
        Mockito.doReturn(TestConstants.USER_1).when(userService).loggedInUser();
        Mockito.doReturn(Optional.of(TestConstants.ACCOUNT_2)).when(accountRepository).findById(ID);
        // When
        Executable result = () -> accountService.deleteAccountById(ID);
        // Then
        org.junit.jupiter.api.Assertions.assertThrows(ServerException.class, result);
    }
    @Test
    void givenIdWhenDeleteAccountByIdThenAssertResult() throws ServerException {
        // Given
        Mockito.doReturn(TestConstants.USER_1).when(userService).loggedInUser();
        Mockito.doReturn(Optional.of(TestConstants.ACCOUNT_1)).when(accountRepository).findById(ID);
        // When
        accountService.deleteAccountById(ID);
        // Then
        Mockito.verify(accountRepository).save(TestConstants.ACCOUNT_1);
    }
    @Test
    void givenCurrencyWhenValidateCurrencyThenAssertResult(){
        // When
        Executable result = () -> accountService.validateCurrency(ID);
        // Then
        org.junit.jupiter.api.Assertions.assertThrows(ServerException.class, result);
    }
    @Test
    void givenNewAccountObjectWhenAddNewAccountThenAssertResult() throws ServerException {
        // Given
        RequestSchema.NewAccount newAccount = new RequestSchema.NewAccount();
        newAccount.setName(TestConstants.NAME);
        newAccount.setCurrency(AppConstants.CurrencyEnum.EUR.getCurrency());
        Mockito.doReturn(TestConstants.USER_1).when(userService).loggedInUser();
        Mockito.doReturn(TestConstants.ACCOUNT_1).when(accountRepository).save(Mockito.any(Account.class));
        // When
        Account account = accountService.addNewAccount(newAccount);
        // Then
        Assertions.assertThat(account).isEqualTo(TestConstants.ACCOUNT_1);
    }
}
