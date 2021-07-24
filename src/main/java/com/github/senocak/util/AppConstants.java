package com.github.senocak.util;

import lombok.Getter;
import lombok.AllArgsConstructor;

public interface AppConstants {
    String DEFAULT_PAGE_NUMBER = "1";
    String DEFAULT_PAGE_SIZE = "10";
    String MAIL_REGEX = "^\\S+@\\S+\\.\\S+$";
    String CURRENCY_TOKEN = "https://v1.nocodeapi.com/kgvfncwxqnjvykbeey/cx/HJcVLVYmBhHQgZdk/rates";

    @Getter
    @AllArgsConstructor
    enum CurrencyEnum {
        EUR("EUR"),
        TRY("TRY"),
        USD("USD");
        private final String currency;
    }
}
