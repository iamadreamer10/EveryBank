package com.backend.global.exception.handler;

import com.backend.global.exception.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ExceptionHandler {

//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    public ErrorResponse badRequestHandler(RuntimeException exception){
//        log.error(exception.getMessage(), exception);
//    }
}
