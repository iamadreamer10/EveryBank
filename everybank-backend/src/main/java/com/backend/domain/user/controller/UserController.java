package com.backend.domain.user.controller;

import com.backend.domain.user.dto.UserRequestDto;
import com.backend.domain.user.dto.UserResponseDto;
import com.backend.domain.user.service.UserService;
import com.backend.global.common.BaseResponse;
import com.backend.global.common.code.SuccessCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("")
public class UserController {

    private final UserService userService;

    @PostMapping("/join")
    public ResponseEntity<BaseResponse<UserResponseDto>> join(@RequestBody UserRequestDto request){
        UserResponseDto userInfo = userService.join(request);
        return BaseResponse.success(SuccessCode.CREATED_USER, userInfo);
    }

    @GetMapping("/email_check/{email}")
    public ResponseEntity<BaseResponse<Map<String, Boolean>>> checkEmail(@PathVariable String email){
        boolean isExists = userService.checkEmail(email);
        Map<String, Boolean> response = Map.of("isAvailable", !isExists);
        SuccessCode status = isExists ? SuccessCode.EMAIL_ALREADY_EXISTS : SuccessCode.EMAIL_AVAILABLE;
        return BaseResponse.success(status, response);
    }
}
