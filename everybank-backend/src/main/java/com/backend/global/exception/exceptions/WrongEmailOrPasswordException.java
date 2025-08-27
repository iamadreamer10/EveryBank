package com.backend.global.exception.exceptions;

import org.springframework.web.reactive.function.client.WebClientResponseException;

public class WrongEmailOrPasswordException extends RuntimeException {

    public WrongEmailOrPasswordException(String message) {
        super(message);
    }
}
