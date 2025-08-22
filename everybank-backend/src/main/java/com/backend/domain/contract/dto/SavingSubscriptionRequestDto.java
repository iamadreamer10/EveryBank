package com.backend.domain.contract.dto;

import lombok.Data;

@Data
public class SavingSubscriptionRequestDto {
    private String productCode;
    private Long monthlyAmount;
    private Long optionId;
}
