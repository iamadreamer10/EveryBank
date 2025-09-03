package com.backend.domain.contract.dto;

import com.backend.domain.account.domain.AccountType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class MaturityCalculationDto {
    // 계좌 기본 정보
    private Integer accountId;
    private AccountType accountType;
    private String productName;
    private String companyName;

    // 계약 정보
    private LocalDate contractDate;    // 계약일
    private LocalDate maturityDate;    // 만기일
    private Integer saveTerm;          // 저축기간 (개월)
    private BigDecimal interestRate;   // 기본금리
    private BigDecimal interestRate2;  // 우대금리
    private String interestRateTypeName; // 금리유형 (단리/복리)

    // 적금 전용 정보 (적금인 경우만)
    private Long monthlyPayment;       // 월 납입액
    private Integer totalPaymentCount; // 총 납입 횟수
    private Integer currentPaymentCount; // 현재 납입 횟수

    // 만기정산 계산 결과
    private Long totalPrincipal;            // 원금 총액
    private Long totalInterest;             // 이자 총액
    private Long totalPayout;          // 총 지급액 (원금+이자)

    // 현재 상태
    private Long currentCheckAmount;   // 현재 입출금계좌 잔액
    private boolean isMatured;         // 만기 도래 여부
}
