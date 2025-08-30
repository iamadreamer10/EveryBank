package com.backend.domain.account.dto;

import lombok.Data;

@Data
public class CheckingAccountRequestDto {
    private String companyCode;
    private String bankName;
}