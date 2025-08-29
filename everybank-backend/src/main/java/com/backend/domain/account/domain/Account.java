package com.backend.domain.account.domain;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "account")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "account_seq")
    @SequenceGenerator(name = "account_seq", sequenceName = "account_id_seq", allocationSize = 1)
    @Column(name = "id")
    private int id;

    @Column(name = "company_code", length = 40, nullable = false)
    private String companyCode;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "current_balance", nullable = false)
    private Long currentBalance;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", length = 15, nullable = false)
    private AccountType accountType;

    @Column(name = "maturity_date", nullable = false)
    private LocalDate maturityDate;

    @Column(name = "last_transaction_date")
    private LocalDateTime lastTransactionDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_state", length = 20, nullable = false)
    private AccountState accountState;


}
