package com.github.senocak.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;
import javax.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Where;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "accounts")
@EntityListeners(Account.AccountListeners.class)
@Where(clause = "deleted=false")
public class Account extends DateAudit {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", updatable = false, nullable = false)
    private String id;

    @Column
    private String name;

    @Column
    private String currency;

    @Column
    private BigDecimal balance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JsonManagedReference
    @JoinColumn(name = "USER_ID")
    private User user;

    private boolean deleted = Boolean.FALSE;

    public static class AccountListeners {
        @PrePersist
        public void pre(final Account account) {
            account.setId(UUID.randomUUID().toString());
        }
    }
}
