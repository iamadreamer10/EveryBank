package com.backend.domain.contract.controller;

import com.backend.domain.contract.dto.*;
import com.backend.domain.contract.service.ContractDetailService;
import com.backend.domain.contract.service.ContractService;
import com.backend.global.common.BaseResponse;
import com.backend.global.common.code.SuccessCode;
import com.backend.global.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/contract")
public class ContractController {

    private final ContractService contractService;
    private final ContractDetailService contractDetailService;


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

    @GetMapping("/{accountId}/maturity")
    public ResponseEntity<BaseResponse<MaturityCalculationDto>> calculateMaturity(
            @PathVariable Integer accountId,
            @AuthenticationPrincipal SecurityUser securityUser) {
        MaturityCalculationDto calculation = contractService.calculateMaturity(accountId, securityUser);
        return BaseResponse.success(SuccessCode.SELECT_SUCCESS, calculation);
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<BaseResponse<ContractDetailResponseDto>> getContractDetailByAccount(
            @PathVariable Integer accountId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @AuthenticationPrincipal SecurityUser securityUser) {

        ContractDetailResponseDto contractDetail = contractDetailService.getContractDetail(
                accountId, securityUser, page, size);

        return BaseResponse.success(SuccessCode.SELECT_SUCCESS, contractDetail);
    }

}
