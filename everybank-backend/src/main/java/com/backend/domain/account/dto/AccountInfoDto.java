package com.backend.domain.account.dto;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class AccountInfoDto {
    private int accountId;
    private String accountName;      // 계좌명
    private Long balance;            // 현재 잔액
    private String bank;            // 은행명
    private String productName;     // 상품명
    private String accountType;     // CHECK/DEPOSIT/SAVING
    private String startDate;       // yyyy.MM.dd 형식
    private String endDate;         // yyyy.MM.dd 형식
    private String status;          // active/expired/early_closed

    // 적금 전용 필드들 (적금이 아닌 경우 null)
    private Long monthlyPayment;           // 월 납입액
    private Integer currentPaymentCount;   // 현재 납입 횟수
    private Integer totalPaymentCount;     // 총 납입 예정 횟수
    private String nextPaymentDate;        // 다음 납입 예정일 (yyyy-MM-dd)

    // 예금 전용 필드들 (예금이 아닌 경우 null)
    private Long totalDepositAmount;       // 예금 총 납입액
}