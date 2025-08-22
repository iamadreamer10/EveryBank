package com.backend.domain.contract.dto;

import lombok.Data;

@Data
public class DepositSubscriptionRequestDto {
    private String productCode;
    private Long totalAmount;
    private Long optionId;
}
