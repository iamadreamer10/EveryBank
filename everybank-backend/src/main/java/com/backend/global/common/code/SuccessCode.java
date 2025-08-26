package com.backend.global.common.code;

import lombok.Data;
import lombok.Getter;

@Getter
public enum SuccessCode {

    SELECT_SUCCESS(200, "200", "SELECT SUCCESS"),
    EMAIL_ALREADY_EXISTS(200, "200", "이미 사용중인 이메일입니다."),
    EMAIL_AVAILABLE(200, "200", "사용 가능한 이메일입니다."),

    // 삭제 성공 코드 (HTTP Response: 200 OK)
    DELETE_SUCCESS(200, "200", "DELETE SUCCESS"),
    LOGIN_SUCCESS(200, "200", "LOGIN SUCCESS"),
    LOGOUT_SUCCESS(200, "200", "LOGOUT SUCCESS"),
    SIGNOUT_SUCCESS(200, "200", "SIGN OUT SUCCESS"),

    // 삽입 성공 코드 (HTTP Response: 201 Created)
    INSERT_SUCCESS(201, "201", "INSERT SUCCESS"),
    CREATE_SUCCESS(201, "201", "CREATE SUCCESS"),
    CREATED_USER(201, "201", "회원가입 성공"),


    // 수정 성공 코드 (HTTP Response: 201 Created)
    UPDATE_SUCCESS(201, "201", "UPDATE SUCCESS");

    private final int status;
    private final String code;
    private final String message;

    SuccessCode(final int status, final String code, final String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
