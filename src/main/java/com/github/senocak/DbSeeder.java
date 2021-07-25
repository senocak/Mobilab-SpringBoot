package com.github.senocak;

import java.math.BigDecimal;
import java.util.UUID;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.senocak.exception.ServerException;
import com.github.senocak.payload.RequestSchema;
import com.github.senocak.service.TransferService;
import com.github.senocak.util.AppConstants;
import org.springframework.boot.CommandLineRunner;
import lombok.extern.slf4j.Slf4j;
import com.github.javafaker.Faker;
import com.github.senocak.model.*;
import org.springframework.beans.factory.annotation.Value;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.github.senocak.repository.*;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DbSeeder implements CommandLineRunner {
    @Value("${app.db}")
    private String environment;
    private final Faker faker = new Faker();
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountRepository accountRepository;
    private final TransferService transferService;

    @Override
    public void run(String... strings) throws ServerException, JsonProcessingException {
        if(!environment.equals("dev")){
            log.info("app.db is not in dev mode, skip the seeders");
            return;
        }
        log.info("Adding some dummy datas");
        User user1 = User.builder().username("anil1").name(faker.name().firstName()).email("anil1@senocak.com").password(passwordEncoder.encode("anil1")).build();
        User user2 = User.builder().username("anil2").name(faker.name().firstName()).email("anil2@senocak.com").password(passwordEncoder.encode("anil2")).build();
        userRepository.save(user1);
        userRepository.save(user2);

        Account account1 = Account.builder().name("TR Account").currency(AppConstants.CurrencyEnum.TRY.getCurrency()).balance(new BigDecimal(100)).user(user1).build();
        accountRepository.save(account1);
        Account account2 = Account.builder().name("EUR Account").currency(AppConstants.CurrencyEnum.EUR.getCurrency()).balance(new BigDecimal(200)).user(user1).build();
        accountRepository.save(account2);
        Account account3 = Account.builder().name("USD Account").currency(AppConstants.CurrencyEnum.USD.getCurrency()).balance(new BigDecimal(300)).user(user2).build();
        accountRepository.save(account3);
        Account account4 = Account.builder().name("EUR Account").currency(AppConstants.CurrencyEnum.EUR.getCurrency()).balance(new BigDecimal(400)).user(user2).build();
        accountRepository.save(account4);

        RequestSchema.Transfer transfer1 = new RequestSchema.Transfer();
        transfer1.setAmount(new BigDecimal(1));
        transfer1.setCurrency(AppConstants.CurrencyEnum.EUR.getCurrency());
        transfer1.setToAccountId(UUID.fromString(account1.getId()));
        transfer1.setFromAccountId(UUID.fromString(account3.getId()));
        transferService.create(transfer1.getCurrency(), transfer1.getAmount(), transfer1.getFromAccountId(), transfer1.getToAccountId());

        RequestSchema.Transfer transfer2 = new RequestSchema.Transfer();
        transfer2.setAmount(new BigDecimal(2));
        transfer2.setCurrency(AppConstants.CurrencyEnum.EUR.getCurrency());
        transfer2.setToAccountId(UUID.fromString(account1.getId()));
        transfer2.setFromAccountId(UUID.fromString(account3.getId()));
        transferService.create(transfer2.getCurrency(), transfer2.getAmount(), transfer2.getFromAccountId(), transfer2.getToAccountId());

        RequestSchema.Transfer transfer3 = new RequestSchema.Transfer();
        transfer3.setAmount(new BigDecimal(2));
        transfer3.setCurrency(AppConstants.CurrencyEnum.EUR.getCurrency());
        transfer3.setToAccountId(UUID.fromString(account3.getId()));
        transfer3.setFromAccountId(UUID.fromString(account1.getId()));
        transferService.create(transfer3.getCurrency(), transfer3.getAmount(), transfer3.getFromAccountId(), transfer3.getToAccountId());

        log.info("Seeding completed");
    }
}
