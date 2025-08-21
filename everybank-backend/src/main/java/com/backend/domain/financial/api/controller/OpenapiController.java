package com.backend.domain.financial.api.controller;

import com.backend.domain.financial.api.service.FinancialProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OpenapiController implements ApplicationRunner {

    private final FinancialProductService financialProductService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("=== 금융 Open API 데이터 로딩 시작 ===");

        try {
            financialProductService.loadAllData();
            log.info("=== 금융상품 데이터 로딩 완료 ===");

        } catch (Exception e) {
            log.error("금융상품 데이터 로딩 실패", e);
        }
    }
}