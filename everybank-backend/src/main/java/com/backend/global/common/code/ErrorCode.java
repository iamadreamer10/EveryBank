package com.backend.global.common.code;


import lombok.Getter;

@Getter
public enum ErrorCode {
    /**
     * * ******************************* Global Error CodeList ***************************************
     * * HTTP Status Code
     * * 400 : Bad Request
     * * 401 : Unauthorized
     * * 403 : Forbidden
     * * 404 : Not Found
     * * 409 : Conflict
     */

    // 잘못된 요청
    BAD_REQUEST_ERROR(400, "400", "Bad Request Exception"),

    // 권한 없음
    UNAUTHORIZED_ERROR(401, "401", "Unauthorized Exception"),
    WRONG_EMAIL_OR_PASSWORD(401, "401", "잘못된 이메일/비밀번호 입니다."),

    // 금지됨
    FORBIDDEN_ERROR(403, "403", "Forbidden Exception"),

    // 찾을 수 없음
    NOT_FOUND_ERROR(404, "404", "Not Found Exception"),

    // 충돌
    CONFLICT_ERROR(409, "409", "Conflict Exception");

    private final int status;
    private final String code;
    private final String message;

    ErrorCode(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
