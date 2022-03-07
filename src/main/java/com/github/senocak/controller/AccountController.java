package com.github.senocak.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.senocak.exception.ServerException;
import com.github.senocak.model.Account;
import com.github.senocak.model.Transfer;
import com.github.senocak.model.User;
import com.github.senocak.payload.RequestSchema;
import com.github.senocak.payload.ResponseSchema;
import com.github.senocak.service.AccountService;
import com.github.senocak.service.TransferService;
import com.github.senocak.service.UserService;
import com.github.senocak.util.JsonSchemaValidator;
import com.github.senocak.util.ErrorMessageType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.*;
import static com.github.senocak.util.AppConstants.DEFAULT_PAGE_NUMBER;
import static com.github.senocak.util.AppConstants.DEFAULT_PAGE_SIZE;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
@CrossOrigin(origins = "*", maxAge = 3600)
@Api(value = "AccountController", description = "Account Controller")
public class AccountController {
    // comment
    private final UserService userService;
    private final TransferService transferService;
    private final AccountService accountService;
    private final ModelMapper modelMapper;
    private final JsonSchemaValidator jsonSchemaValidator;
    private final ResponseSchema response = new ResponseSchema(true, null);

    @GetMapping("/me")
    @ApiOperation(value = "Get User Info", response = ResponseSchema.class, tags = {"user"})
    public ResponseEntity<ResponseSchema> getUser() throws ServerException {
        User user = userService.loggedInUser();
        log.info("Logged in user is: {}", user);
        ResponseSchema.UserProfile userProfile = modelMapper.map(user, ResponseSchema.UserProfile.class);
        log.info("userProfile: {}", userProfile);
        response.setMessage(userProfile);
        return ResponseEntity.ok(response);
    }
    @PatchMapping("/update")
    @ApiOperation(value = "Update Profile", response = ResponseSchema.class, tags = {"user"})
    public ResponseEntity<ResponseSchema> patchUser(@RequestBody RequestSchema.UserUpdateProfile userUpdateProfile) throws ServerException {
        jsonSchemaValidator.validateJsonSchema(userUpdateProfile, RequestSchema.UserUpdateProfile.class);
        User user = userService.patchUser(userUpdateProfile);
        log.info("Logged in user is: {}", user);
        ResponseSchema.UserProfile userProfile = modelMapper.map(user, ResponseSchema.UserProfile.class);
        log.info("userProfile: {}", userProfile);
        response.setMessage(userProfile);
        return ResponseEntity.ok(response);
    }
    @DeleteMapping("/{accountId}")
    @ApiOperation(value = "Delete Account By Id", response = ResponseSchema.class, tags = {"user"})
    public ResponseEntity<ResponseSchema> deleteAccount(@PathVariable String accountId) throws ServerException {
        accountService.deleteAccountById(accountId);
        response.setMessage(new String[]{"Account Deleted"});
        return ResponseEntity.ok(response);
    }
    @PostMapping("/addAccount")
    @ApiOperation(value = "Add New Account", response = ResponseSchema.class, tags = {"user"})
    public ResponseEntity<ResponseSchema> addAccount(@RequestBody RequestSchema.NewAccount newAccount) throws ServerException {
        jsonSchemaValidator.validateJsonSchema(newAccount, RequestSchema.NewAccount.class);
        accountService.validateCurrency(newAccount.getCurrency());
        Account addNewAccount = accountService.addNewAccount(newAccount);
        ResponseSchema.AccountResponse accountResponse = modelMapper.map(addNewAccount, ResponseSchema.AccountResponse.class);
        response.setMessage(accountResponse);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    @PostMapping("/send")
    @ApiOperation(value = "Send Money", response = ResponseSchema.class, tags = {"user"})
    public ResponseEntity<ResponseSchema> postTransfer(@RequestBody RequestSchema.Transfer transfer) throws ServerException, JsonProcessingException {
        jsonSchemaValidator.validateJsonSchema(transfer, RequestSchema.Transfer.class);
        transferService.validateTransferObject(transfer);
        Transfer transferSaved = transferService.create(
            transfer.getCurrency(),
            transfer.getAmount(),
            transfer.getFromAccountId(),
            transfer.getToAccountId()
        );
        log.info("transfer is completed: {}", transfer);
        response.setMessage(modelMapper.map(transferSaved, ResponseSchema.TransferResponse.class));
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    @GetMapping("/{accountId}/{transferType}")
    @ApiOperation(value = "Get All Transfer For Account", response = ResponseSchema.class, tags = {"user"})
    public ResponseEntity<ResponseSchema> getTransfersForAccount(@PathVariable String accountId, @PathVariable String transferType,
        @RequestParam(defaultValue = DEFAULT_PAGE_NUMBER) Integer page,
        @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) Integer size,
        @RequestParam(defaultValue = "asc") String by,
        @RequestParam(defaultValue = "") String fromDate,
        @RequestParam(defaultValue = "") String toDate) throws ServerException {
        if (!transferType.equals("incomings") && !transferType.equals("outgoings")){
            log.error("Transfer should only be incomings or outgoings. Provided: {}", transferType);
            throw new ServerException(
                ErrorMessageType.BASIC_INVALID_INPUT, new String[]{"Transfer Type:" + transferType}, HttpStatus.BAD_REQUEST);
        }
        if (page < 1 || size < 1){
            log.error("Page: {} or Size: {} not valid.", page, size);
            throw new ServerException(
                ErrorMessageType.BASIC_INVALID_INPUT, new String[]{"Page: " + page, "Size: " + size}, HttpStatus.BAD_REQUEST);
        }
        if (!by.equals("asc") && !by.equals("desc")){
            log.error("Ordering Format must be asc or desc. Provided: {}", by);
            throw new ServerException(ErrorMessageType.BASIC_INVALID_INPUT, new String[]{"Order: "+ by}, HttpStatus.BAD_REQUEST);
        }
        Sort sort = by.equals("desc") ? Sort.by("createdAt").descending() : Sort.by("createdAt").ascending();
        Pageable paging = PageRequest.of(page - 1, size, sort);
        Instant from = !fromDate.isEmpty() ? Instant.parse(fromDate) : null;
        Instant to = !toDate.isEmpty() ? Instant.parse(toDate) : null;

        ResponseSchema.PagedTransferResponse findAll = transferService.getAllForAccount(transferType, accountId, paging, from, to);
        response.setMessage(modelMapper.map(findAll, ResponseSchema.PagedTransferResponse.class));
        return ResponseEntity.ok(response);
    }
}
