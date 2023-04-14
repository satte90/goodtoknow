package com.teliacompany.webflux.request.metrics;

import com.teliacompany.webflux.request.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;

import javax.annotation.PostConstruct;

public class DisabledMetricsReporter implements MetricsReporter {
    private static final Logger LOG = LoggerFactory.getLogger(DisabledMetricsReporter.class);

    @PostConstruct
    public void init() {
        LOG.warn("Metrics reporting disabled. " +
                "If you require metrics reporting make sure you have micrometer-registry-prometheus dependency " +
                "and you have metrics.prefix property set");
    }

    @Override
    public void reportRequestData(RequestContext context, HttpStatus httpStatus) {
        //Do nothing
    }

    @Override
    public void reportRequestError(RequestContext requestContext, ClientResponse clientResponse) {
        //Do nothing
    }

    @Override
    public void reportRequestError(RequestContext requestContext, Throwable e) {
        //Do nothing
    }
}
