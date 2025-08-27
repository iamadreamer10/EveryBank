package com.backend.domain.user.controller;

import com.backend.domain.user.dto.LoginDto;
import com.backend.domain.user.dto.PasswordChangeDto;
import com.backend.domain.user.dto.UserRequestDto;
import com.backend.domain.user.dto.UserResponseDto;
import com.backend.domain.user.service.UserService;
import com.backend.global.common.BaseResponse;
import com.backend.global.common.code.SuccessCode;
import com.backend.global.security.SecurityUser;
import com.backend.global.security.util.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("")
public class UserController {

    private final UserService userService;

    @PostMapping("/join")
    public ResponseEntity<BaseResponse<UserResponseDto>> join(@RequestBody UserRequestDto request) {
        UserResponseDto userInfo = userService.join(request);
        return BaseResponse.success(SuccessCode.CREATED_USER, userInfo);
    }

    @PostMapping("/login")
    public ResponseEntity<BaseResponse<Map<String, String>>> login(@RequestBody LoginDto request) {
        Map<String, String> tokenMap = userService.login(request);

        return BaseResponse.success(SuccessCode.LOGIN_SUCCESS, tokenMap);
    }

    @PostMapping("/service-logout")
    public ResponseEntity<BaseResponse<Void>> logout(
            HttpServletRequest request,
            @AuthenticationPrincipal SecurityUser securityUser) {
        log.info("=== LOGOUT ENDPOINT CALLED ===");
        String accessToken = SecurityUtil.getAccessToken(request);
        log.info("securityUser: {}", securityUser);
        userService.logout(accessToken, securityUser);
        return BaseResponse.success(SuccessCode.LOGOUT_SUCCESS, null);
    }

    @GetMapping("/email_check/{email}")
    public ResponseEntity<BaseResponse<Map<String, Boolean>>> checkEmail(@PathVariable String email) {
        boolean isExists = userService.checkEmail(email);
        Map<String, Boolean> response = Map.of("isAvailable", !isExists);
        SuccessCode status = isExists ? SuccessCode.EMAIL_ALREADY_EXISTS : SuccessCode.EMAIL_AVAILABLE;
        return BaseResponse.success(status, response);
    }

    @PatchMapping("/password_change")
    public ResponseEntity<BaseResponse<Void>> changePassword(
            @RequestBody PasswordChangeDto request,
            @AuthenticationPrincipal SecurityUser securityUser) {

        log.info("Changing password for user {}", securityUser);

        userService.passwordCorrectionCheck(request, securityUser);
        userService.setPassword(request.getNewPassword(), securityUser.getId());
        return BaseResponse.success(SuccessCode.PASSWORD_CHANGE_SUCCESS, null);
    }
}
