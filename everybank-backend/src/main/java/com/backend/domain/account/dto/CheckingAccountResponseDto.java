package com.backend.domain.account.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CheckingAccountResponseDto {
    private int accountId;
    private String companyCode;
    private String bankName;
    private String accountType;
    private Long currentBalance;
    private String accountState;
    private LocalDateTime contractDate;
}