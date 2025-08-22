package com.backend.domain.product.dto.response;

import com.backend.domain.product.domain.SavingProduct;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SavingProductDetailDto {
    // 상품 정보 (전체)
    private SavingProduct productInfo;

    private List<SavingProductOptionDto> options;

    @Data
    public static class SavingProductOptionDto {
        private Long id;
        private char interestRateType;
        private String interestRateTypeName;
        private int saveTerm;
        private char reverseType;
        private String reverseTypeName;
        private BigDecimal interestRate;
        private BigDecimal interestRate2;
    }

    public SavingProductDetailDto(SavingProduct productInfo, List<SavingProductOptionDto> options) {
        this.productInfo = productInfo;
        this.options = options;
    }
}
