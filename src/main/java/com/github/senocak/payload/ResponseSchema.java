package com.github.senocak.payload;

import lombok.*;
import java.math.BigDecimal;
import java.util.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.builder.ToStringBuilder;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Getter
@Setter
public class ResponseSchema {
    private Boolean success;
    private Object message;

    public ResponseSchema(Boolean success, Object message) {
        this.success = success;
        this.message = message;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UserProfile {
        private String name;
        private String email;
        private String username;
        private Set<AccountResponse> accounts;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AccountResponse {
        private String id;
        private String name;
        private String currency;
        private BigDecimal balance;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TransferResponse {
        private String currency;
        private BigDecimal amount;
        private AccountResponseForTransfer fromAccountId;
        private AccountResponseForTransfer toAccountId;
        private String createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AccountResponseForTransfer {
        private String id;
        private String name;
        private String currency;
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PagedTransferResponse<T> {
        private List<TransferResponse> content;
        private int page;
        private long totalElements;
        private int totalPages;
        private boolean previous;
        private boolean next;
        private String sort;
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CurrencyResponseFromAPI<T> {
        private String timestamp;
        private String source;
        private String date;
        private Map<String, String> rates;
    }
}
