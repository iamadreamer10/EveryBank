package com.backend.domain.account.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CheckingBalanceDto {
    private int accountId;
    private Long currentBalance;
    private String bankName;
    private String lastTransactionDate;  // "yyyy-MM-dd HH:mm:ss" 형식
}