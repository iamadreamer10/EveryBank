package com.backend.domain.account.dto;

import com.backend.domain.account.domain.AccountState;
import com.backend.domain.account.domain.AccountType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class AccountInfoDto {
    private int id;

    private String companyCode;

    private Long userId;

    private Long currentBalance;

    private AccountType accountType;

    private LocalDate maturityDate;

    private LocalDateTime lastTransactionDate;

    private AccountState accountState;
}
