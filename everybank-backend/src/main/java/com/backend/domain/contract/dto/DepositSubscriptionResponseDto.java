package com.backend.domain.contract.dto;

import com.backend.domain.product.domain.DepositProductOption;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class DepositSubscriptionResponseDto {
    private Long contractId;

    private Long userId;

    private String nickname;

    private String productCode;

    private String productName;

    private String companyName;

    private Long totalAmount;

    private LocalDate contractDate;

    private LocalDate maturityDate;

    private DepositProductOptionResponseDto option;
}
