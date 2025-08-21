package com.backend.domain.product.dto;

import com.backend.domain.product.domain.DepositProduct;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class DepositProductDetailDto {

    private DepositProduct productInfo;

    private List<DepositProductOptionDto> options;

    @Data
    public static class DepositProductOptionDto {
        private Long id;
        private char interestRateType;
        private String interestRateTypeName;
        private int saveTerm;
        private BigDecimal interestRate;
        private BigDecimal interestRate2;
    }

    public DepositProductDetailDto(DepositProduct productInfo, List<DepositProductOptionDto> options) {
        this.productInfo = productInfo;
        this.options = options;
    }
}
