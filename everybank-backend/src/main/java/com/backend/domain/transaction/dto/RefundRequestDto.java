package com.backend.domain.transaction.dto;

import lombok.Data;

@Data
public class RefundRequestDto {
    private Integer fromAccountId;
    private Long totalAmount;
}