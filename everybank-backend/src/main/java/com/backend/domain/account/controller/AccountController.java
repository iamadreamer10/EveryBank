package com.backend.domain.account.controller;

import com.backend.domain.account.dto.AccountInfoDto;
import com.backend.domain.account.dto.MyAccountListInfoDto;
import com.backend.domain.account.service.AccountService;
import com.backend.global.common.BaseResponse;
import com.backend.global.common.code.SuccessCode;
import com.backend.global.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/my_account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping("")
    public ResponseEntity<BaseResponse<MyAccountListInfoDto>> getMyAccounts(@AuthenticationPrincipal SecurityUser securityUser) {
        Long id =  securityUser.getId();
        MyAccountListInfoDto myAccounts = accountService.getMyAccounts(id);
        return BaseResponse.success(SuccessCode.SELECT_SUCCESS, myAccounts);
    }

}
