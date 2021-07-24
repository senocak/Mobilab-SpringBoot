package com.github.senocak.model;

import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;
import javax.persistence.*;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@Table(name = "transfers", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"id"})
})
@AllArgsConstructor
@EntityListeners(Transfer.TransferListeners.class)
public class Transfer extends DateAudit{
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", updatable = false, nullable = false)
    private String id;

    @Column
    private String currency;

    @Column
    private BigDecimal amount;

    @OneToOne
    @JoinColumn(name = "from_account_id", nullable = false)
    private Account fromAccountId;

    @OneToOne
    @JoinColumn(name = "to_account_id", nullable = false)
    private Account toAccountId;

    public static class TransferListeners {
        @PrePersist
        public void onPrePersist(final Transfer transfer) {
            transfer.setId(UUID.randomUUID().toString());
        }
    }
}
