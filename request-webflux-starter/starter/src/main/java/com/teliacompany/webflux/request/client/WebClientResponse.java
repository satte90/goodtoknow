package com.teliacompany.webflux.request.client;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import java.util.Optional;
import java.util.StringJoiner;


public class WebClientResponse<T> {
    private final T body;
    private final HttpHeaders headers;
    private final HttpStatus statusCode;

    WebClientResponse(T body, HttpHeaders headers, HttpStatus statusCode) {
        this.body = body;
        this.headers = headers;
        this.statusCode = statusCode;
    }

    public Optional<T> getBody() {
        return Optional.ofNullable(body);
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    public HttpStatus getStatusCode() {
        return statusCode;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", WebClientResponse.class.getSimpleName() + "[", "]")
                .add("body=" + body)
                .add("headers=" + headers)
                .add("statusCode=" + statusCode)
                .toString();
    }
}
