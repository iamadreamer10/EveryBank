package com.backend.domain.contract.controller;

import com.backend.domain.contract.dto.DepositSubscriptionRequestDto;
import com.backend.domain.contract.dto.DepositSubscriptionResponseDto;
import com.backend.domain.contract.dto.SavingSubscriptionRequestDto;
import com.backend.domain.contract.dto.SavingSubscriptionResponseDto;
import com.backend.domain.contract.service.ContractService;
import com.backend.global.common.BaseResponse;
import com.backend.global.common.code.SuccessCode;
import com.backend.global.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/contract")
public class ContractController {

    private final ContractService contractService;


    @PostMapping("/deposit")
    public ResponseEntity<BaseResponse<DepositSubscriptionResponseDto>> subscribeDeposit(@RequestBody DepositSubscriptionRequestDto requestDto,
                                                                                         @AuthenticationPrincipal SecurityUser securityUser) {
        DepositSubscriptionResponseDto depositContract = contractService.subscribeDeposit(requestDto, securityUser);
        return BaseResponse.success(SuccessCode.CREATE_SUCCESS, depositContract);
    }

    @PostMapping("/saving")
    public ResponseEntity<BaseResponse<SavingSubscriptionResponseDto>> subscribeSaving(@RequestBody SavingSubscriptionRequestDto requestDto,
                                                                                       @AuthenticationPrincipal SecurityUser securityUser) {
        SavingSubscriptionResponseDto savingContract = contractService.subscribeSaving(requestDto, securityUser);
        return BaseResponse.success(SuccessCode.CREATE_SUCCESS, savingContract);
    }

}
