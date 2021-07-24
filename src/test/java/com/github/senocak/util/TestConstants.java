package com.github.senocak.util;

import com.github.senocak.model.Account;
import com.github.senocak.model.Transfer;
import com.github.senocak.model.User;
import com.github.senocak.payload.ResponseSchema;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.UUID;

public class TestConstants {
    private TestConstants() {
        throw new IllegalStateException("Constants class");
    }
    public static final String EMAIL = "anil1@senocak.com";
    public static final String NAME = "anil";
    public static final String PASS = "anil1";
    public static final String CURRENCY = AppConstants.CurrencyEnum.TRY.getCurrency();
    public static final BigDecimal AMOUNT = new BigDecimal(1);
    public static final Account ACCOUNT_1 = Account.builder().id(UUID.randomUUID().toString()).name(NAME).balance(AMOUNT).currency(CURRENCY).build();
    public static final Account ACCOUNT_2 = Account.builder().id(UUID.randomUUID().toString()).name(NAME).balance(AMOUNT).currency(CURRENCY).build();
    public static final User USER_1 = User.builder().username(NAME).name(NAME).email(EMAIL).password("$2a$10$dhZyj.GogYmGEnTSGVWQPOiRGj.u/jLEHPZzwBvBENoKVd.B26ley").accounts(Collections.singletonList(ACCOUNT_1)).build();
    public static final User USER_2 = User.builder().username(NAME).name(NAME).email(EMAIL).password("$2a$10$dhZyj.GogYmGEnTSGVWQPOiRGj.u/jLEHPZzwBvBENoKVd.B26ley").accounts(Collections.singletonList(ACCOUNT_2)).build();
    public static final ResponseSchema.AccountResponse ACCOUNT_RESPONSE = ResponseSchema.AccountResponse.builder().name(NAME).balance(AMOUNT).currency(CURRENCY).build();
    public static final ResponseSchema.UserProfile USER_PROFILE = ResponseSchema.UserProfile.builder().username(NAME).email(EMAIL).name(NAME).accounts(Collections.singleton(ACCOUNT_RESPONSE)).build();
    public static final Transfer
        TRANSFER = Transfer.builder().currency(CURRENCY).amount(AMOUNT).fromAccountId(ACCOUNT_1).toAccountId(ACCOUNT_2).build();
    public static final ResponseSchema.AccountResponseForTransfer FROM_ACCOUNT_RESPONSE_FOR_TRANSFER = ResponseSchema.AccountResponseForTransfer.builder().id(ACCOUNT_1.getId()).name(NAME).currency(CURRENCY).build();
    public static final ResponseSchema.AccountResponseForTransfer TO_ACCOUNT_RESPONSE_FOR_TRANSFER = ResponseSchema.AccountResponseForTransfer.builder().id(ACCOUNT_2.getId()).name(NAME).currency(CURRENCY).build();
    public static final ResponseSchema.TransferResponse TRANSFER_RESPONSE = ResponseSchema.TransferResponse.builder().amount(TestConstants.AMOUNT).currency(CURRENCY).fromAccountId(FROM_ACCOUNT_RESPONSE_FOR_TRANSFER).toAccountId(TO_ACCOUNT_RESPONSE_FOR_TRANSFER).build();
    public static final ResponseSchema.PagedTransferResponse<?> PAGED_TRANSFER_RESPONSE = ResponseSchema.PagedTransferResponse.builder().content(Collections.singletonList(TRANSFER_RESPONSE)).page(1).totalElements(1).totalPages(1).previous(false).next(false).sort("asc").build();
    public static final String TOKEN = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxIiwiaWF0IjoxNjI2ODY5OTM5LCJleHAiOjE2Mjc0NzQ3Mzl9.8lFaVMiwRNvq4NwmO1k9Ls6qE1xi07YUHL2C2cJ6F0OuQ90zh_3GvRQ-2D6HulJn4H09ZhlgffounVgjBFJt6w";
}
