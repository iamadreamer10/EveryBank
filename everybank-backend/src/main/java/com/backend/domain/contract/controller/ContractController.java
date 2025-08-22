package com.backend.domain.contract.controller;

import com.backend.domain.contract.dto.DepositSubscriptionRequestDto;
import com.backend.domain.contract.dto.DepositSubscriptionResponseDto;
import com.backend.domain.contract.dto.SavingSubscriptionRequestDto;
import com.backend.domain.contract.dto.SavingSubscriptionResponseDto;
import com.backend.domain.contract.service.ContractService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<DepositSubscriptionResponseDto> subscribeDeposit(@RequestBody DepositSubscriptionRequestDto requestDto) {
        DepositSubscriptionResponseDto depositContract = contractService.subscribeDeposit(requestDto);
        return ResponseEntity.ok(depositContract);
    }

    @PostMapping("/saving")
    public ResponseEntity<SavingSubscriptionResponseDto> subscribeSaving(@RequestBody SavingSubscriptionRequestDto requestDto) {
        SavingSubscriptionResponseDto savingContract = contractService.subscribeSaving(requestDto);
        return ResponseEntity.ok(savingContract);
    }

}
