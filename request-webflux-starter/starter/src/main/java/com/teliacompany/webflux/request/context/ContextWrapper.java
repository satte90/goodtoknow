package com.teliacompany.webflux.request.context;

import com.teliacompany.webflux.request.log.trace.TraceLogger;

public class ContextWrapper {
    private final TransactionContext transactionContext;
    private final RequestContext requestContext;
    private final TraceLogger traceLogger;

    public ContextWrapper(TransactionContext transactionContext, RequestContext requestContext, TraceLogger traceLogger) {
        this.transactionContext = transactionContext;
        this.requestContext = requestContext;
        this.traceLogger = traceLogger;
    }

    public TransactionContext getTransactionContext() {
        return transactionContext;
    }

    public RequestContext getRequestContext() {
        return requestContext;
    }

    public TraceLogger getTraceLogger() {
        return traceLogger;
    }
}
