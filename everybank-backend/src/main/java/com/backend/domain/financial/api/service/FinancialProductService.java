package com.backend.domain.financial.api.service;

import com.backend.domain.company.domain.FinCompany;
import com.backend.domain.company.dto.CompanyDto;
import com.backend.domain.financial.api.dto.FinancialApiResponse;
import com.backend.domain.financial.mapper.FinancialDataMapper;
import com.backend.domain.company.repository.FinCompanyRepository;
import com.backend.domain.product.domain.*;
import com.backend.domain.financial.api.dto.DepositProductDto;
import com.backend.domain.financial.api.dto.DepositProductOptionDto;
import com.backend.domain.financial.api.dto.SavingProductDto;
import com.backend.domain.financial.api.dto.SavingProductOptionDto;
import com.backend.domain.product.repository.DepositProductOptionRepository;
import com.backend.domain.product.repository.DepositProductRepository;
import com.backend.domain.product.repository.SavingProductOptionRepository;
import com.backend.domain.product.repository.SavingProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FinancialProductService {
    private final WebClient webClient;
    private final FinancialDataMapper dataMapper;


    // Repository들
    private final FinCompanyRepository finCompanyRepository;
    private final DepositProductRepository depositProductRepository;
    private final SavingProductRepository savingProductRepository;
    private final DepositProductOptionRepository depositOptionRepository;
    private final SavingProductOptionRepository savingOptionRepository;

    @Value("${finlifeapi.url}")
    private String apiUrl;

    @Value("${finlifeapi.auth}")
    private String apiKey;

    public void loadAllData() {
        deleteAllData();

        // 1. 금융회사 정보 먼저 로드
        loadCompanies();

        // 2. 예금상품 로드
        loadDepositProducts();
        updateMainRates();
        // 3. 적금상품 로드
        loadSavingProducts();
        updateSavingMainRates();
    }

    @Transactional
    public void deleteAllData() {
        // 외래키 순서에 맞춰 자식부터 삭제
        depositOptionRepository.deleteAll();
        savingOptionRepository.deleteAll();
        depositProductRepository.deleteAll();
        savingProductRepository.deleteAll();
        finCompanyRepository.deleteAll();

        System.out.println("모든 데이터 삭제 완료");
    }

    private void loadCompanies() {
        String url = apiUrl + "/companySearch.json?auth=" + apiKey + "&topFinGrpNo=020000&pageNo=1";
        FinancialApiResponse<CompanyDto, Void> response = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<FinancialApiResponse<CompanyDto, Void>>() {})
                .timeout(Duration.ofSeconds(30))
                .block();

        for (CompanyDto dto : response.getResult().getBaseList()) {
            FinCompany company = FinCompany.builder()
                                .companyName(dto.getKorCoNm())
                                .companyCode(dto.getFinCoNo())
                                .build();
            finCompanyRepository.save(company);
        }
    }

    private void loadDepositProducts() {
        String url = apiUrl + "/depositProductsSearch.json?auth=" + apiKey + "&topFinGrpNo=020000&pageNo=1";
        FinancialApiResponse<DepositProductDto, DepositProductOptionDto> response = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<FinancialApiResponse<DepositProductDto, DepositProductOptionDto>>() {})
                .timeout(Duration.ofSeconds(30))
                .block();

        processDepositData(response);
    }

    private void processDepositData(FinancialApiResponse<DepositProductDto, DepositProductOptionDto> response) {
        // 2. 상품 저장
        for (DepositProductDto dto : response.getResult().getBaseList()) {
            DepositProduct product = dataMapper.toEntity(dto);
            depositProductRepository.save(product);
        }

        // 3. 옵션 저장
        for (DepositProductOptionDto dto : response.getResult().getOptionList()) {
            DepositProduct product = depositProductRepository.findByProductCode(dto.getFinPrdtCd());
            DepositProductOption option = dataMapper.toEntity(dto, product);
            if (option.getInterestRate2().compareTo(BigDecimal.ZERO) == 0) {
                option.setInterestRate2(option.getInterestRate());
            }
            depositOptionRepository.save(option);
        }

        updateMainRates();
    }


    private void loadSavingProducts() {
        String url = apiUrl + "/savingProductsSearch.json?auth=" + apiKey + "&topFinGrpNo=020000&pageNo=1";
        FinancialApiResponse<SavingProductDto, SavingProductOptionDto> response = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<FinancialApiResponse<SavingProductDto, SavingProductOptionDto>>() {})
                .timeout(Duration.ofSeconds(30))
                .block();

        processSavingData(response);
    }

    private void processSavingData(FinancialApiResponse<SavingProductDto, SavingProductOptionDto> response) {
        // 적금상품 저장 로직
        // 2. 상품 저장
        for (SavingProductDto dto : response.getResult().getBaseList()) {
            SavingProduct product = dataMapper.toEntity(dto);
            savingProductRepository.save(product);
        }

        // 3. 옵션 저장
        for (SavingProductOptionDto dto : response.getResult().getOptionList()) {
            SavingProduct product = savingProductRepository.findByProductCode(dto.getFinPrdtCd());
            SavingProductOption option = dataMapper.toEntity(dto, product);
            if (option.getInterestRate2().compareTo(BigDecimal.ZERO) == 0) {
                option.setInterestRate2(option.getInterestRate());
            }
            savingOptionRepository.save(option);
        }
    }

    private <T, O> FinancialApiResponse<T, O> callAPI(String endpoint) {
        String requestUrl = endpoint + "?auth=" + apiKey + "&topFinGrpNo=020000&pageNo=1";

        return webClient.get()
                .uri(requestUrl)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<FinancialApiResponse<T, O>>() {})
                .timeout(Duration.ofSeconds(30))
                .block();
    }

    private void updateMainRates() {
        List<DepositProduct> products = depositProductRepository.findAll();

        for (DepositProduct product : products) {
            List<DepositProductOption> options =
                    depositOptionRepository.findByDepositProductProductCode(product.getProductCode());

            // 최대 금리 찾기
            BigDecimal mainRate = options.stream()
                    .map(DepositProductOption::getInterestRate2)
                    .filter(rate -> rate != null && rate.compareTo(BigDecimal.ZERO) > 0)
                    .max(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);

            product.setMainRate(mainRate);
            depositProductRepository.save(product);
        }
    }


    private void updateSavingMainRates() {
        List<SavingProduct> products = savingProductRepository.findAll();

        for (SavingProduct product : products) {
            List<SavingProductOption> options =
                    savingOptionRepository.findBySavingProductProductCode(product.getProductCode());

            // 최대 금리 찾기
            BigDecimal mainRate = options.stream()
                    .map(SavingProductOption::getInterestRate2)
                    .filter(rate -> rate != null && rate.compareTo(BigDecimal.ZERO) > 0)
                    .max(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);

            product.setMainRate(mainRate);
            savingProductRepository.save(product);
        }
    }

}
