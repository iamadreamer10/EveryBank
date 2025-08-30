package com.backend.domain.transaction.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TransactionResponseDto {
    private Long transactionId;
    private String transactionType;
    private Long amount;
    private Integer fromAccountId;
    private Integer toAccountId;
    private LocalDateTime createdAt;
    private Long checkingAccountBalance;
}