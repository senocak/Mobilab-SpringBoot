package com.github.senocak.service;

import com.github.senocak.exception.ServerException;
import com.github.senocak.model.Account;
import com.github.senocak.model.User;
import com.github.senocak.payload.RequestSchema;
import com.github.senocak.repository.AccountRepository;
import com.github.senocak.util.AppConstants;
import com.github.senocak.util.OmaErrorMessageType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final UserService userService;

    public Account findById(String id) throws ServerException {
        return accountRepository.findById(id).orElseThrow(() -> new ServerException(OmaErrorMessageType.NOT_FOUND, new String[]{"Account", "id", id}, HttpStatus.NOT_FOUND));
    }
    public Account saveAccount(Account account){
        return accountRepository.save(account);
    }
    public void deleteAccountById(String id) throws ServerException {
        User user = userService.loggedInUser();
        Account account = findById(id);
        if (!user.getAccounts().contains(account))
            throw new ServerException(OmaErrorMessageType.BASIC_INVALID_INPUT, new String[]{"Account does not belong this user"}, HttpStatus.BAD_REQUEST);
        account.setDeleted(true);
        accountRepository.save(account);
    }
    public void validateCurrency(String currency) throws ServerException {
        boolean valid = false;
        for (AppConstants.CurrencyEnum type : AppConstants.CurrencyEnum.values()) {
            if (type.name().equals(currency)) {
                valid = true;
                break;
            }
        }
        if (!valid)
            throw new ServerException(OmaErrorMessageType.BASIC_INVALID_INPUT, new String[]{"Curreny is not supported", currency}, HttpStatus.BAD_REQUEST);
    }
    public Account addNewAccount(RequestSchema.NewAccount newAccount) throws ServerException {
        Account addNewAccount = Account.builder()
            .name(newAccount.getName())
            .balance(new BigDecimal(0))
            .currency(newAccount.getCurrency())
            .user(userService.loggedInUser())
            .build();
        return accountRepository.save(addNewAccount);
    }
}
