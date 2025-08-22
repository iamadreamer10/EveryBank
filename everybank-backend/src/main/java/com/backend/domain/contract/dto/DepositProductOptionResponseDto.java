package com.backend.domain.contract.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DepositProductOptionResponseDto {
    private Long id;
    private char interestRateType;
    private String interestRateTypeName;
    private int saveTerm;
    private BigDecimal interestRate;
    private BigDecimal interestRate2;
}
