package com.teliacompany.webflux.request.context;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.util.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Request context to be stored in SubscriberContext in the webflux chain
 */
public class RequestContext {
    private static final Logger LOG = LoggerFactory.getLogger(RequestContext.class);
    private final String serviceName;
    private final ContextHttpRequest request;
    private final long startTime;
    private final String requestId;
    private Map<String, String> metaData = new HashMap<>();

    // Used to keep track of number of calls within the transaction/request.
    private int requestIdCounter;
    private long requestDuration;

    RequestContext(String serviceName, ContextHttpRequest contextHttpRequest, Map<String, String> metaData, int requestIdCounter) {
        this.serviceName = serviceName;
        this.request = contextHttpRequest;
        this.startTime = System.currentTimeMillis();
        this.requestIdCounter = requestIdCounter;
        this.requestId = String.valueOf(requestIdCounter);
        if(metaData != null) {
            this.metaData = metaData;
        }
    }

    static TransactionContext newTransaction(String tid, ContextHttpRequest contextHttpRequest, String tcad, String tscId, boolean tracingEnabled) {
        LOG.debug("Creating new transaction context with tid: {}", tid);
        return new TransactionContext(tid, contextHttpRequest, tcad, tscId, tracingEnabled);
    }

    /**
     * parentRequestContext can either be the transactionContext or another requestContext. It may not be null, if that's the case use newTransaction(tid...)
     */
    static RequestContext newRequest(String serviceName, ContextHttpRequest contextHttpRequest, @NonNull TransactionContext transactionContext) {
        return new RequestContext(serviceName, contextHttpRequest, transactionContext.getMetaData(), transactionContext.incrementAndGetMessageIdCounter());
    }

    public ContextHttpRequest getRequest() {
        return request;
    }

    public long getStartTime() {
        return startTime;
    }

    /**
     * @return the new requestId counter
     */
    int incrementAndGetMessageIdCounter() {
        requestIdCounter = requestIdCounter + 1;
        return requestIdCounter;
    }

    public int getRequestIdCounter() {
        return requestIdCounter;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setMetaData(Map<String, String> metaData) {
        this.metaData = metaData;
    }

    public Map<String, String> getMetaData() {
        if(metaData == null) {
            metaData = new HashMap<>();
        }
        return metaData;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void recordRequestDuration() {
        this.requestDuration = System.currentTimeMillis() - this.startTime;
        LOG.debug("Recorded request duration = {}", requestDuration);
    }

    public long getRequestDuration() {
        return requestDuration;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("serviceName", serviceName)
                .append("request", request)
                .append("startTime", startTime)
                .append("requestId", requestId)
                .append("metaData", metaData)
                .append("requestIdCounter", requestIdCounter)
                .append("requestDuration", requestDuration)
                .toString();
    }
}
