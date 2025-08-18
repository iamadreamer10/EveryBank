package com.backend.global.exception.response;

import jakarta.validation.constraints.NotNull;

public class ErrorResponse {

    @NotNull
    private final int status;

    @NotNull
    private final String message;

    public ErrorResponse(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
