package com.backend.global.exception.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    BAD_REQUEST(400, "잘못된 요청입니다."),
    UNAUTHORIZED(401, "사용자 정보를 찾을 수 없습니다.");

    private final int status;
    private final String message;
}
