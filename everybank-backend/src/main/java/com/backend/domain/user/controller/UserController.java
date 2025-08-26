package com.backend.domain.user.controller;

import com.backend.domain.user.dto.UserRequestDto;
import com.backend.domain.user.dto.UserResponseDto;
import com.backend.domain.user.service.UserService;
import com.backend.global.common.BaseResponse;
import com.backend.global.common.code.SuccessCode;
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
@RequestMapping("")
public class UserController {

    private final UserService userService;

    @PostMapping("/join")
    public ResponseEntity<BaseResponse<UserResponseDto>> join(@RequestBody UserRequestDto request){
        UserResponseDto userInfo = userService.join(request);
        return BaseResponse.success(SuccessCode.CREATED_USER, userInfo);
    }
}
