package com.backend.domain.account.controller;

import com.backend.domain.account.dto.AccountDetailResponseDto;
import com.backend.domain.account.dto.CheckingAccountRequestDto;
import com.backend.domain.account.dto.CheckingAccountResponseDto;
import com.backend.domain.account.dto.MyAccountListInfoDto;
import com.backend.domain.account.service.AccountService;
import com.backend.domain.transaction.dto.*;
import com.backend.global.common.BaseResponse;
import com.backend.global.common.code.SuccessCode;
import com.backend.global.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/my_account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping("")
    public ResponseEntity<BaseResponse<MyAccountListInfoDto>> getMyAccounts(@AuthenticationPrincipal SecurityUser securityUser) {
        Long id = securityUser.getId();
        MyAccountListInfoDto myAccounts = accountService.getMyAccounts(id);
        return BaseResponse.success(SuccessCode.SELECT_SUCCESS, myAccounts);
    }

    // 📋 가입계좌 상세조회 API
    @GetMapping("/{accountId}")
    public ResponseEntity<BaseResponse<AccountDetailResponseDto>> getAccountDetail(
            @PathVariable Integer accountId,
            @AuthenticationPrincipal SecurityUser securityUser) {
        AccountDetailResponseDto accountDetail = accountService.getAccountDetail(accountId, securityUser);
        return BaseResponse.success(SuccessCode.SELECT_SUCCESS, accountDetail);
    }

    // 입출금계좌 등록 API
    @PostMapping("/check/register")
    public ResponseEntity<BaseResponse<CheckingAccountResponseDto>> registerCheckingAccount(
            @RequestBody CheckingAccountRequestDto requestDto,
            @AuthenticationPrincipal SecurityUser securityUser) {
        CheckingAccountResponseDto checkingAccount = accountService.registerCheckingAccount(requestDto, securityUser);
        return BaseResponse.success(SuccessCode.CREATE_SUCCESS, checkingAccount);
    }

    // 외부 → 입출금계좌 (외부입금)
    @PostMapping("/deposit")
    public ResponseEntity<BaseResponse<TransactionResponseDto>> externalDeposit(
            @RequestBody ExternalDepositRequestDto requestDto,
            @AuthenticationPrincipal SecurityUser securityUser) {
        TransactionResponseDto transaction = accountService.externalDeposit(requestDto, securityUser);
        return BaseResponse.success(SuccessCode.CREATE_SUCCESS, transaction);
    }

    // 입출금계좌 → 외부 (외부출금)
    @PostMapping("/withdraw")
    public ResponseEntity<BaseResponse<TransactionResponseDto>> externalWithdraw(
            @RequestBody ExternalWithdrawRequestDto requestDto,
            @AuthenticationPrincipal SecurityUser securityUser) {
        TransactionResponseDto transaction = accountService.externalWithdraw(requestDto, securityUser);
        return BaseResponse.success(SuccessCode.CREATE_SUCCESS, transaction);
    }

    // 입출금계좌 → 금융상품 (납입)
    @PostMapping("/payment")
    public ResponseEntity<BaseResponse<TransactionResponseDto>> paymentToProduct(
            @RequestBody PaymentRequestDto requestDto,
            @AuthenticationPrincipal SecurityUser securityUser) {
        TransactionResponseDto transaction = accountService.paymentToProduct(requestDto, securityUser);
        return BaseResponse.success(SuccessCode.CREATE_SUCCESS, transaction);
    }

    // 금융상품 → 입출금계좌 (환급)
    @PostMapping("/refund")
    public ResponseEntity<BaseResponse<TransactionResponseDto>> refundFromProduct(
            @RequestBody RefundRequestDto requestDto,
            @AuthenticationPrincipal SecurityUser securityUser) {
        TransactionResponseDto transaction = accountService.refundFromProduct(requestDto, securityUser);
        return BaseResponse.success(SuccessCode.CREATE_SUCCESS, transaction);
    }



}