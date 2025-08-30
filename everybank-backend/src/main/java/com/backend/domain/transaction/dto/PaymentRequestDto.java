package com.backend.domain.transaction.dto;

import lombok.Data;

@Data
public class PaymentRequestDto {
    private Integer toAccountId;
    private Long amount;
}
