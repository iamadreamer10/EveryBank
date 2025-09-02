package com.backend.domain.company.controller;

import com.backend.domain.company.dto.CompanyResponseDto;
import com.backend.domain.company.repository.FinCompanyRepository;
import com.backend.global.common.BaseResponse;
import com.backend.global.common.code.SuccessCode;
import com.backend.global.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@RestController
@RequestMapping("/company")
@RequiredArgsConstructor
public class FinCompanyController {

    private final FinCompanyRepository finCompanyRepository;

    @GetMapping("")
    public ResponseEntity<BaseResponse<List<CompanyResponseDto>>> getCompany(@AuthenticationPrincipal SecurityUser securityUser) {
        log.info("SecurityUser={}", securityUser);
        List<CompanyResponseDto> companies = finCompanyRepository.findAll().stream()
                .map(company -> new CompanyResponseDto(company.getCompanyCode(), company.getCompanyName()))
                .collect(Collectors.toList());
        log.info("companies={}", companies);
        return BaseResponse.success(SuccessCode.SELECT_SUCCESS, companies);
    }
}
