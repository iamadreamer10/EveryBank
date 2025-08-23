package com.backend.global.exception.handler;

import com.backend.global.common.BaseResponse;
import com.backend.global.common.code.ErrorCode;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.naming.AuthenticationException;
import java.nio.file.AccessDeniedException;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 400 - Bad Request
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<BaseResponse<String>> handleBadRequestException(IllegalArgumentException e) {
        log.error("Bad Request Exception: {}", e.getMessage(), e);
        return BaseResponse.error(ErrorCode.BAD_REQUEST_ERROR, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<String>> handleValidationException(MethodArgumentNotValidException e) {
        log.error("Validation Exception: {}", e.getMessage(), e);
        String errorMessage = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return BaseResponse.error(ErrorCode.BAD_REQUEST_ERROR, errorMessage);
    }

    // 401 - Unauthorized
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<BaseResponse<String>> handleUnauthorizedException(AuthenticationException e) {
        log.error("Unauthorized Exception: {}", e.getMessage(), e);
        return BaseResponse.error(ErrorCode.UNAUTHORIZED_ERROR, e.getMessage());
    }

    // 403 - Forbidden
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<BaseResponse<String>> handleForbiddenException(AccessDeniedException e) {
        log.error("Forbidden Exception: {}", e.getMessage(), e);
        return BaseResponse.error(ErrorCode.FORBIDDEN_ERROR, e.getMessage());
    }

    // 404 - Not Found
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<BaseResponse<String>> handleNotFoundException(NoSuchElementException e) {
        log.error("Not Found Exception: {}", e.getMessage(), e);
        return BaseResponse.error(ErrorCode.NOT_FOUND_ERROR, e.getMessage());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<BaseResponse<String>> handleEntityNotFoundException(EntityNotFoundException e) {
        log.error("Entity Not Found Exception: {}", e.getMessage(), e);
        return BaseResponse.error(ErrorCode.NOT_FOUND_ERROR, e.getMessage());
    }

    // 409 - Conflict
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<BaseResponse<String>> handleConflictException(DataIntegrityViolationException e) {
        log.error("Conflict Exception: {}", e.getMessage(), e);
        return BaseResponse.error(ErrorCode.CONFLICT_ERROR, "데이터 충돌이 발생했습니다.");
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<BaseResponse<String>> handleDuplicateKeyException(DuplicateKeyException e) {
        log.error("Duplicate Key Exception: {}", e.getMessage(), e);
        return BaseResponse.error(ErrorCode.CONFLICT_ERROR, "중복된 데이터입니다.");
    }

    // 500 - Internal Server Error (일반적인 예외)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<String>> handleGeneralException(Exception e) {
        log.error("Internal Server Error: {}", e.getMessage(), e);
        return ResponseEntity
                .status(500)
                .body(new BaseResponse<>("서버 내부 오류가 발생했습니다.", 500, "Internal Server Error"));
    }

    // Runtime Exception
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<BaseResponse<String>> handleRuntimeException(RuntimeException e) {
        log.error("Runtime Exception: {}", e.getMessage(), e);
        return ResponseEntity
                .status(500)
                .body(new BaseResponse<>("런타임 오류가 발생했습니다.", 500, "Runtime Error"));
    }
}
