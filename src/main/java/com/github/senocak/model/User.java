package com.github.senocak.model;

import lombok.*;
import java.util.List;
import javax.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"username"}),
    @UniqueConstraint(columnNames = {"email"})
})
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User extends DateAudit {
    @Id
    @JsonIgnore
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column
    private String name;

    @Column
    private String username;

    @Column
    private String email;

    @Column
    @JsonIgnore
    private String password;

    @Column
    @JsonBackReference
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    private List<Account> accounts;
}
