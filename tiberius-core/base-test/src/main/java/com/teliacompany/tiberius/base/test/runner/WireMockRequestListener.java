package com.teliacompany.tiberius.base.test.runner;

import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.Request;
import com.teliacompany.tiberius.base.server.api.TiberiusHeaders;
import com.teliacompany.tiberius.base.test.logger.TestLoggerFactory;

import java.util.logging.Logger;

public class WireMockRequestListener {
    private static final Logger LOG = TestLoggerFactory.getLogger(TiberiusTestAppBootstrapper.class);

    protected static void requestReceived(Request inRequest, com.github.tomakehurst.wiremock.http.Response inResponse) {
        HttpHeader requestId = inRequest.getHeaders().getHeader(TiberiusHeaders.X_TRANSACTION_ID);
        LOG.info("WireMock request (" + requestId + ") at URL: " + inRequest.getAbsoluteUrl());
        LOG.finer("WireMock request headers: \n" + inRequest.getHeaders());
        LOG.finer("WireMock response body: \n" + inResponse.getBodyAsString());
        LOG.finer("WireMock response headers: \n" + inResponse.getHeaders());
    }
}
