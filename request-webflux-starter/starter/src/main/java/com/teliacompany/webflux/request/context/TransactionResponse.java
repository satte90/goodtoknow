package com.teliacompany.webflux.request.context;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import java.util.List;

public final class TransactionResponse {
    private HttpStatus httpStatus;
    private HttpHeaders httpHeaders = new HttpHeaders();
    private Object body;

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public TransactionResponse setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
        return this;
    }

    public HttpHeaders getHttpHeaders() {
        return httpHeaders;
    }

    public TransactionResponse setHttpHeader(String key, String value) {
        this.httpHeaders.add(key, value);
        return this;
    }

    public TransactionResponse setHttpHeader(String key, List<String> values) {
        values.forEach(value -> this.httpHeaders.add(key, value));
        return this;
    }

    public TransactionResponse setHttpHeaders(HttpHeaders httpHeaders) {
        this.httpHeaders = httpHeaders;
        return this;
    }

    public Object getBody() {
        return body;
    }

    public TransactionResponse setBody(Object body) {
        this.body = body;
        return this;
    }
}
