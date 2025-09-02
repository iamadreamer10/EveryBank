package com.backend.domain.account.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ContractInfo {
    private String productName;
    private LocalDate contractDate;
    private LocalDate endDate;
}