package com.teliacompany.webflux.request.log;

import com.teliacompany.webflux.request.context.RequestContext;
import com.teliacompany.webflux.request.context.TransactionResponse;
import org.springframework.web.reactive.function.client.ClientResponse;

import java.util.Map;

public class DisabledRequestLogger implements RequestLogger {


    @Override
    public void logResponse(RequestContext requestContext, ClientResponse clientResponse, Object payload, RequestLoggingOptions loggingOptions) {
        // Do nothing
    }

    @Override
    public void logResponse(RequestContext requestContext, TransactionResponse transactionResponse, RequestLoggingOptions loggingOptions) {
        // Do nothing
    }

    @Override
    public void logError(RequestContext requestContext, Throwable e, RequestLoggingOptions loggingOptions) {
        // Do nothing
    }

    @Override
    public void logRequest(RequestContext context, String payload, RequestLoggingOptions loggingOptions) {
        // Do nothing
    }

    @Override
    public void logRequest(RequestContext context, byte[] payload, RequestLoggingOptions loggingOptions) {
        // Do nothing
    }

    @Override
    public void logInternalRequest(RequestContext context, RequestLoggingOptions loggingOptions) {
        // Do nothing
    }

    @Override
    public void logMessage(RequestLoggingOptions loggingOptions, Map<String, Object> map) {
        // Do nothing
    }
}
