package com.backend.global.exception.exceptions;

public class InvalidPasswordException extends RuntimeException {
    public InvalidPasswordException(String message) {
        super(message);
    }

    public InvalidPasswordException() {
        super("현재 비밀번호가 일치하지 않습니다.");
    }
}
