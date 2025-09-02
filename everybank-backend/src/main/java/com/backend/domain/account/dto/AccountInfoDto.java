package com.backend.domain.account.dto;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class AccountInfoDto {
    private int accountId;
    private String accountName;      // 새로 추가
    private Long balance;
    private String bank;            // companyName
    private String productName;     // 상품명 (입출금계좌는 "입출금계좌")
    private String accountType;     // CHECK/DEPOSIT/SAVING
    private String startDate;       // yyyy.MM.dd 형식
    private String endDate;
    private String status;          // active/expired/early_closed
}
