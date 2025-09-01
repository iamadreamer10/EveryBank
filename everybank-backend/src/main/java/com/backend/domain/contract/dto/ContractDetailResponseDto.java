package com.backend.domain.contract.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ContractDetailResponseDto {

    private ContractInfoDto contractInfo;
    private AccountInfoDto accountInfo;
    private ExpectedAmountsDto expectedAmounts;
    private List<TransactionDetailDto> transactions;
    private PaginationDto pagination;


    @Data
    @Builder
    public static class ContractInfoDto {
        private Long contractId;
        private String productCode;
        private String productName;
        private String bank;                    // companyName
        private String contractType;            // "SAVING" or "DEPOSIT"
        private Double interestRate;            // 우대금리
        private String interestRateType;        // "단리" or "복리"
        private Long monthlyPayment;            // 적금만 (예금은 null)
        private Long totalAmount;               // 예금만 (적금은 null)
        private Integer term;                   // 저축기간 (개월)
        private LocalDate startDate;            // 계약일
        private LocalDate endDate;              // 만기일
        private String contractStatus;          // "ACTIVE", "MATURED", etc
    }

    @Data
    @Builder
    public static class AccountInfoDto {
        private Integer accountId;
        private String accountNumber;           // 향후 구현 예정
        private Long currentBalance;            // 현재 잔액
        private Integer paymentCount;           // 적금 납입 횟수 (예금은 null)
        private LocalDateTime lastTransactionDate;
    }

    @Data
    @Builder
    public static class ExpectedAmountsDto {
        private Long totalPayment;              // 총 납입 예정액 (적금) or 납입액 (예금)
        private Long expectedInterest;          // 예상 이자
        private Long maturityAmount;            // 만기 시 수령액
    }

    @Data
    @Builder
    public static class TransactionDetailDto {
        private Long transactionId;
        private LocalDateTime transactionDate;  // createdAt
        private String transactionType;         // "PAYMENT", "TRANSFER", etc
        private Long amount;
        private Long balance;                   // 거래 후 해당 계좌 잔액
        private String description;             // 거래 설명
        private Integer paymentNumber;          // 적금 납입 회차 (적금만)
        private String memo;                    // "8회차 | 25년 08월분"
    }

    @Data
    @Builder
    public static class PaginationDto {
        private Integer currentPage;
        private Integer totalPages;
        private Long totalCount;
        private Boolean hasNext;
    }

}