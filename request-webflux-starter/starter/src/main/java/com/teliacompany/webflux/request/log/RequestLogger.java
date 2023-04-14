package com.teliacompany.webflux.request.log;

import com.teliacompany.webflux.request.context.RequestContext;
import com.teliacompany.webflux.request.context.TransactionResponse;
import org.springframework.web.reactive.function.client.ClientResponse;

import java.util.Map;

public interface RequestLogger {
    void logResponse(RequestContext requestContext, ClientResponse clientResponse, Object payload, RequestLoggingOptions loggingOptions);

    void logResponse(RequestContext requestContext, TransactionResponse transactionResponse, RequestLoggingOptions loggingOptions);

    void logError(RequestContext requestContext, Throwable e, RequestLoggingOptions loggingOptions);

    void logRequest(RequestContext context, String payload, RequestLoggingOptions loggingOptions);

    void logRequest(RequestContext context, byte[] payload, RequestLoggingOptions loggingOptions);

    void logInternalRequest(RequestContext context, RequestLoggingOptions loggingOptions);

    void logMessage(RequestLoggingOptions loggingOptions, Map<String, Object> map);
}
