package com.teliacompany.webflux.request.client;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

public class WebClientMockResponse<T> extends WebClientResponse<T> {
    public WebClientMockResponse(T body, HttpHeaders headers, HttpStatus statusCode) {
        super(body, headers, statusCode);
    }
}
