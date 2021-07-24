package com.github.senocak.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.senocak.exception.ServerException;
import com.github.senocak.model.Transfer;
import com.github.senocak.model.User;
import com.github.senocak.payload.RequestSchema;
import com.github.senocak.payload.ResponseSchema;
import com.github.senocak.repository.TransferRepository;
import com.github.senocak.util.ErrorMessageType;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import lombok.RequiredArgsConstructor;
import com.github.senocak.model.Account;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import org.springframework.web.client.RestTemplate;
import static com.github.senocak.util.AppConstants.CURRENCY_TOKEN;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferService {
    private final TransferRepository transferRepository;
    private final AccountService accountService;
    private final ModelMapper modelMapper;
    private final UserService userService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public void validateTransferObject(RequestSchema.Transfer transfer) throws ServerException, JsonProcessingException {
        User user = userService.loggedInUser();
        Account from = accountService.findById(transfer.getFromAccountId().toString());
        log.info("From Account is valid: {}", from);
        if (!user.getAccounts().contains(from))
            throw new ServerException(ErrorMessageType.BASIC_INVALID_INPUT, new String[]{"User does not have access this account"}, HttpStatus.UNAUTHORIZED);
        if (from.getBalance().compareTo(new BigDecimal(0)) <= 0)
            throw new ServerException(ErrorMessageType.BASIC_INVALID_INPUT, new String[]{"Not valid money is found in sender account"}, HttpStatus.BAD_REQUEST);
        BigDecimal convertedAmount = convertCurrency(transfer.getCurrency(), transfer.getAmount(), from.getCurrency());
        if (from.getBalance().compareTo(convertedAmount) < 0)
            throw new ServerException(ErrorMessageType.BASIC_INVALID_INPUT, new String[]{"Can not send more than you have"}, HttpStatus.BAD_REQUEST);
        Account to = accountService.findById(transfer.getToAccountId().toString());
        log.info("To Account is valid: {}", to);
        accountService.validateCurrency(transfer.getCurrency());
    }
    public Transfer create(String currency, BigDecimal amount, UUID fromAccountId, UUID toAccountId)
        throws ServerException, JsonProcessingException {
        Account to = accountService.findById(toAccountId.toString());
        Account from = accountService.findById(fromAccountId.toString());
        Transfer
            transfer = Transfer.builder().currency(currency).amount(amount).fromAccountId(from).toAccountId(to).build();
        Transfer transferSaved = transferRepository.save(transfer);
        onPostPersist(transfer);
        return transferSaved;
    }
    private BigDecimal convertCurrency(String currency, BigDecimal amount, String toCurrency) throws JsonProcessingException {
        String url = CURRENCY_TOKEN + "?source="+currency+"&target="+toCurrency;
        log.info(url);
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);
        log.info(responseEntity.getBody());
        ResponseSchema.CurrencyResponseFromAPI<?> currencyResponseFromAPI = objectMapper.readValue(responseEntity.getBody(), ResponseSchema.CurrencyResponseFromAPI.class);
        return new BigDecimal(currencyResponseFromAPI.getRates().get(toCurrency)).multiply(amount);
    }
    private void onPostPersist(final Transfer transfer) throws JsonProcessingException, ServerException {
        Account from = accountService.findById(transfer.getFromAccountId().getId());
        from.setBalance(from.getBalance().subtract(convertCurrency(transfer.getCurrency(), transfer.getAmount(), from.getCurrency())));
        accountService.saveAccount(from);

        Account to = accountService.findById(transfer.getToAccountId().getId());
        to.setBalance(to.getBalance().add(convertCurrency(transfer.getCurrency(), transfer.getAmount(), to.getCurrency())));
        accountService.saveAccount(to);
    }
    public ResponseSchema.PagedTransferResponse<?> getAllForAccount(String transferType, String accountId, Pageable paging, Instant fromDate, Instant toDate) throws ServerException {
        User user = userService.loggedInUser();
        Account account = accountService.findById(accountId);
        if (!user.getAccounts().contains(account))
            throw new ServerException(ErrorMessageType.BASIC_INVALID_INPUT, new String[]{"User does not have access this account", accountId}, HttpStatus.UNAUTHORIZED);
        Page<Transfer> pagedResult;
        boolean status = Objects.nonNull(fromDate) && Objects.nonNull(toDate);
        if (transferType.equals("incomings")){
            if (status){
                pagedResult = transferRepository.findAllByToAccountIdAndCreatedAtIsBetween(account, fromDate, toDate, paging);
            }else{
                pagedResult = transferRepository.findAllByToAccountId(account, paging);
            }
        }else{
            if (status){
                pagedResult = transferRepository.findAllByFromAccountIdAndCreatedAtIsBetween(account, fromDate, toDate, paging);
            }else{
                pagedResult = transferRepository.findAllByFromAccountId(account, paging);
            }
        }
        return generateResponse(pagedResult);
    }
    private ResponseSchema.PagedTransferResponse<?> generateResponse(Page<Transfer> pagedResult){
        List<Transfer> getAll = new ArrayList<>();
        if(pagedResult.hasContent()) getAll = pagedResult.getContent();
        List<ResponseSchema.TransferResponse> dtos = getAll.stream()
            .map(transfer -> modelMapper.map(transfer, ResponseSchema.TransferResponse.class))
            .collect(Collectors.toList());
        return ResponseSchema.PagedTransferResponse.builder()
            .content(dtos)
            .page(pagedResult.getNumber() + 1)
            .totalPages(pagedResult.getTotalPages())
            .totalElements(pagedResult.getTotalElements())
            .previous(pagedResult.hasPrevious())
            .next(pagedResult.hasNext())
            .build();
    }
}
