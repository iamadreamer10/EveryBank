package com.backend.domain.product.service;

import com.backend.domain.product.domain.DepositProduct;
import com.backend.domain.product.domain.DepositProductOption;
import com.backend.domain.product.domain.SavingProduct;
import com.backend.domain.product.domain.SavingProductOption;
import com.backend.domain.product.dto.response.DepositProductDetailDto;
import com.backend.domain.contract.dto.DepositSubscriptionResponseDto;
import com.backend.domain.product.dto.response.SavingProductDetailDto;
import com.backend.domain.product.repository.DepositProductOptionRepository;
import com.backend.domain.product.repository.DepositProductRepository;
import com.backend.domain.product.repository.SavingProductOptionRepository;
import com.backend.domain.product.repository.SavingProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final DepositProductRepository depositProductRepository;
    private final SavingProductRepository savingProductRepository;
    private final DepositProductOptionRepository depositProductOptionRepository;
    private final SavingProductOptionRepository savingProductOptionRepository;

    public List<DepositProduct> getDepositProductList() {
        return depositProductRepository.findAll();
    }

    public DepositProductDetailDto getDepositProductDetail(String productCode) {
        DepositProduct productInfo = depositProductRepository.findByProductCode(productCode);
        List<DepositProductOption> options = depositProductOptionRepository.findByDepositProductProductCode(productCode);

        List<DepositProductDetailDto.DepositProductOptionDto> optionDtos = new ArrayList<>();
        for (DepositProductOption option : options) {
            DepositProductDetailDto.DepositProductOptionDto dto = new DepositProductDetailDto.DepositProductOptionDto();
            dto.setId(option.getId());
            dto.setInterestRateType(option.getInterestRateType());
            dto.setInterestRateTypeName(option.getInterestRateTypeName());
            dto.setSaveTerm(option.getSaveTerm());
            dto.setInterestRate(option.getInterestRate());
            dto.setInterestRate2(option.getInterestRate2());
            optionDtos.add(dto);
        }

        return new DepositProductDetailDto(productInfo, optionDtos);
    }

    public List<SavingProduct> getSavingProductList() {
        return savingProductRepository.findAll();
    }

    public SavingProductDetailDto getSavingProductDetail(String productCode) {
        SavingProduct productInfo = savingProductRepository.findByProductCode(productCode);
        List<SavingProductOption> options = savingProductOptionRepository.findBySavingProductProductCode(productCode);

        List<SavingProductDetailDto.SavingProductOptionDto> optionDtos = new ArrayList<>();
        for (SavingProductOption option : options) {
            SavingProductDetailDto.SavingProductOptionDto dto = new SavingProductDetailDto.SavingProductOptionDto();
            dto.setId(option.getId());
            dto.setInterestRateType(option.getInterestRateType());
            dto.setInterestRateTypeName(option.getInterestRateTypeName());
            dto.setSaveTerm(option.getSaveTerm());
            dto.setReverseType(option.getReverseType());
            dto.setReverseTypeName(option.getReverseTypeName());
            dto.setInterestRate(option.getInterestRate());
            dto.setInterestRate2(option.getInterestRate2());
            optionDtos.add(dto);
        }

        return new SavingProductDetailDto(productInfo, optionDtos);
    }
}
