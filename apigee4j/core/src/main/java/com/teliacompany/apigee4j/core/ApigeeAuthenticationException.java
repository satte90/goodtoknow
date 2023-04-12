package com.teliacompany.apigee4j.core;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class ApigeeAuthenticationException extends RuntimeException {
    public ApigeeAuthenticationException(String message) {
        super(message);
    }

    public ApigeeAuthenticationException(String s, RuntimeException e) {
        super(s, e);
    }
}
