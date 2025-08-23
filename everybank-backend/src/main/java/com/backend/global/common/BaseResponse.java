package com.backend.global.common;


import com.backend.global.common.code.ErrorCode;
import com.backend.global.common.code.SuccessCode;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.ResponseEntity;


@Getter
@AllArgsConstructor
@JsonPropertyOrder({"status", "message", "result"})
public class BaseResponse<T> {
    private T result;
    private int status;
    private String message;

    public static <T> ResponseEntity<BaseResponse<T>> success(SuccessCode successCode, T data){
        return ResponseEntity
                .status(successCode.getStatus())
                .body(new BaseResponse<>(data,successCode.getStatus(),successCode.getMessage()));
    }

    public static <T> ResponseEntity<BaseResponse<T>> error(ErrorCode errorCode, T data) {
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(new BaseResponse<>(data, errorCode.getStatus(), errorCode.getMessage()));
    }

}
