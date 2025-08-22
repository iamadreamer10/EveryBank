package com.backend.domain.contract.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class SavingSubscriptionResponseDto {
    private Long contractId;

    private Long userId;

    private String nickname;

    private String productCode;

    private String productName;

    private String companyName;

    private Long monthlyPayment;

    private Integer currentPaymentCount;

    private LocalDate latestPaymentDate;

    private LocalDate contractDate;

    private LocalDate maturityDate;

    private SavingProductOptionResponseDto option;
}
