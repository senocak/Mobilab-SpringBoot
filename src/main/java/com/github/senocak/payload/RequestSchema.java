package com.github.senocak.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import javax.validation.constraints.*;
import com.github.senocak.util.AppConstants;

@Slf4j
public class RequestSchema {
    private RequestSchema(){
      log.error("Private constructor is not supported");
    }

    @Getter
    @Setter
    public static class SignUpRequest {
        @NotBlank
        @Size(min = 4, max = 40)
        private String name;

        @NotBlank
        @Size(min = 3, max = 15)
        private String username;

        @Size(max = 30)
        @Pattern(regexp = AppConstants.MAIL_REGEX)
        private String email;

        @NotBlank
        @Size(min = 5, max = 20)
        private String password;
    }

    @Getter
    @Setter
    public static class LoginRequest {
        @NotBlank
        @Size(min = 3, max = 30)
        private String usernameOrEmail;

        @NotBlank
        @Size(min = 5, max = 20)
        private String password;
    }

    @Getter
    @Setter
    public static class Transfer{
        @NotBlank
        @Size(min = 12, max = 20)
        private UUID fromAccountId;

        @NotBlank
        @Size(min = 12, max = 20)
        private UUID toAccountId;

        @NotBlank
        @Size(min = 3)
        private String currency;

        @DecimalMin(value = "0.0", inclusive = false)
        @Digits(integer=3, fraction=2)
        private BigDecimal amount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UserUpdateProfile {
        private String name;
        private String email;
        private String username;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NewAccount {
        private String name;
        private String currency;
    }
}
