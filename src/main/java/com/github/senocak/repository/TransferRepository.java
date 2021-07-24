package com.github.senocak.repository;

import com.github.senocak.model.Account;
import com.github.senocak.model.Transfer;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import java.time.Instant;

@Repository
public interface TransferRepository extends PagingAndSortingRepository<Transfer, String> {
    Page<Transfer> findAllByToAccountId(Account toAccountId, Pageable pageable);
    Page<Transfer> findAllByFromAccountId(Account toAccountId, Pageable pageable);
    Page<Transfer> findAllByToAccountIdAndCreatedAtIsBetween(Account toAccountId, Instant createdAt, Instant createdAt2, Pageable pageable);
    Page<Transfer> findAllByFromAccountIdAndCreatedAtIsBetween(Account toAccountId, Instant createdAt, Instant createdAt2, Pageable pageable);
}
