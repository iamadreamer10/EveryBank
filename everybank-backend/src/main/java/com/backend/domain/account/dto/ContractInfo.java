package com.backend.domain.account.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ContractInfo {
    private String productName;
    private LocalDate contractDate;
    private LocalDate endDate;

    // 적금 계약 정보 추가
    private Long monthlyPayment;
    private Integer currentPaymentCount;
    private Integer totalPaymentCount;

    // 예금 계약 정보 추가
    private Long totalAmount;
}