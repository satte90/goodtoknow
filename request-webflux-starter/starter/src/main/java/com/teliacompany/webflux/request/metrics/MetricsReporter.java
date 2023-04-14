package com.teliacompany.webflux.request.metrics;

import com.teliacompany.webflux.request.context.RequestContext;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;

public interface MetricsReporter {
    String REQUEST_DURATION_STAT = "request_duration";

    void reportRequestData(RequestContext context, HttpStatus httpStatus);

    void reportRequestError(RequestContext requestContext, ClientResponse clientResponse);

    void reportRequestError(RequestContext requestContext, Throwable e);
}
