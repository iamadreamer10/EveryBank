package com.backend.domain.financial.mapper;

import com.backend.domain.company.domain.FinCompany;
import com.backend.domain.company.dto.CompanyDto;
import com.backend.domain.product.domain.*;
import com.backend.domain.financial.api.dto.DepositProductDto;
import com.backend.domain.financial.api.dto.DepositProductOptionDto;
import com.backend.domain.financial.api.dto.SavingProductDto;
import com.backend.domain.financial.api.dto.SavingProductOptionDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Slf4j
public class FinancialDataMapper {

    public FinCompany toEntity(CompanyDto dto){
        return FinCompany.builder()
                .companyCode(cleanString(dto.getFinCoNo()))
                .companyName(cleanString(dto.getKorCoNm()))
                .build();
    }

    public DepositProduct toEntity(DepositProductDto dto) {
        return DepositProduct.builder()
                .productCode(cleanString(dto.getFinPrdtCd()))
                .productName(cleanString(dto.getFinPrdtNm()))
                .companyCode(cleanString(dto.getFinCoNo()))
                .companyName(cleanString(dto.getKorCoNm()))
                .joinMember(cleanString(dto.getJoinMember()))
                .etcNote(cleanString(dto.getEtcNote()))
                .maxLimit(parseMaxLimit(dto.getMaxLimit()))
                .mainRate(BigDecimal.ZERO) // 기본값, 옵션에서 최고금리로 업데이트
                .build();
    }

    public SavingProduct toEntity(SavingProductDto dto) {
        return SavingProduct.builder()
                .productCode(cleanString(dto.getFinPrdtCd()))
                .productName(cleanString(dto.getFinPrdtNm()))
                .companyCode(cleanString(dto.getFinCoNo()))
                .companyName(cleanString(dto.getKorCoNm()))
                .joinMember(cleanString(dto.getJoinMember()))
                .etcNote(cleanString(dto.getEtcNote()))
                .maxLimit(parseMaxLimit(dto.getMaxLimit()))
                .mainRate(BigDecimal.ZERO) // 기본값, 옵션에서 최고금리로 업데이트
                .build();
    }

    public DepositProductOption toEntity(DepositProductOptionDto dto, DepositProduct product) {
        return DepositProductOption.builder()
                .depositProduct(product)
                .interestRateType(parseRateType(dto.getIntrRateType()))
                .interestRateTypeName(cleanString(dto.getIntrRateTypeNm()))
                .saveTerm(parseSaveTerm(dto.getSaveTrm()))
                .interestRate(parseRate(dto.getIntrRate()))
                .interestRate2(parseRate(dto.getIntrRate2()))
                .build();
    }

    public SavingProductOption toEntity(SavingProductOptionDto dto, SavingProduct product) {
        return SavingProductOption.builder()
                .savingProduct(product)
                .interestRateType(parseRateType(dto.getIntrRateType()))
                .interestRateTypeName(cleanString(dto.getIntrRateTypeNm()))
                .saveTerm(parseSaveTerm(dto.getSaveTrm()))
                .reverseType(parseRateType(dto.getRsrvType()))
                .reverseTypeName(cleanString(dto.getRsrvTypeNm()))
                .interestRate(parseRate(dto.getIntrRate()))
                .interestRate2(parseRate(dto.getIntrRate2()))
                .build();
    }

    private String cleanString(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }

        return input
                .replace("·", "•")                    // 문제되는 가운데점 변경
                .replace("＇", "'")                   // 전각 문자를 반각으로
                .replace("＂", "\"")
                .replaceAll("[\\p{Cntrl}]", "")       // 제어문자 제거
                .trim();
    }

    private char parseRateType(String rateType) {
        if (rateType == null || rateType.trim().isEmpty()) {
            return 'S'; // 기본값: 단리
        }
        return rateType.trim().charAt(0);
    }

    private int parseSaveTerm(String saveTerm) {
        if (saveTerm == null || saveTerm.trim().isEmpty()) {
            return 0;
        }

        try {
            return Integer.parseInt(saveTerm.trim());
        } catch (NumberFormatException e) {
            log.warn("저축기간 파싱 실패: {}", saveTerm);
            return 0;
        }
    }

    private BigDecimal parseRate(String rate) {
        if (rate == null || rate.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }

        try {
            return new BigDecimal(rate.trim());
        } catch (NumberFormatException e) {
            log.warn("금리 파싱 실패: {}", rate);
            return BigDecimal.ZERO;
        }
    }

    private int parseMaxLimit(String maxLimit) {
        if (maxLimit == null || maxLimit.trim().isEmpty()) {
            return 0;
        }

        try {
            // "1억원", "5천만원" 등의 텍스트를 숫자로 변환
            String cleaned = maxLimit.replaceAll("[^0-9]", "");
            return cleaned.isEmpty() ? 0 : Integer.parseInt(cleaned);
        } catch (NumberFormatException e) {
            log.warn("최대한도 파싱 실패: {}", maxLimit);
            return 0;
        }
    }
}