package com.backend.domain.account.dto;

import com.backend.domain.account.domain.AccountType;
import com.backend.domain.contract.domain.ContractCondition;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AccountDetailResponseDto {
    // 기본 계좌 정보
    private Integer accountId;
    private AccountType accountType;
    private String productCode;
    private LocalDate contractDate;
    private LocalDate maturityDate;
    private ContractCondition contractCondition;
    private LocalDateTime lastTransactionDate;

    // 적금 전용 정보 (적금인 경우만)
    private Long monthlyPayment;
    private Integer currentPaymentCount;

    // 예금 전용 정보 (예금인 경우만)
    private Long totalAmount;

    // 상품 옵션 정보
    private ProductOptionDto option;

    // 거래내역
    private List<TransactionDto> transactions;

    @Data
    @Builder
    public static class ProductOptionDto {
        private char interestRateType;
        private String interestRateTypeName;
        private Character reverseType;          // 적금만 (예금은 null)
        private String reverseTypeName;    // 적금만 (예금은 null)
        private Integer saveTerm;
        private Double interestRate;       // BigDecimal → Double 변환
    }

    @Data
    @Builder
    public static class TransactionDto {
        private Long transactionId;
        private String transactionType;
        private Long amount;
        private Integer fromAccountId;
        private Integer toAccountId;
        private LocalDateTime createdAt;
        private Long currentBalance;
    }
}