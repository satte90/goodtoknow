package com.teliacompany.webflux.request.context;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.HashMap;

/**
 * Transaction context to be stored in SubscriberContext in the webflux chain
 */
public class TransactionContext extends RequestContext {
    private final String tid;
    private final String tcad;
    private final String tscid;
    private final boolean tracingEnabled;

    TransactionContext(String tid, ContextHttpRequest contextHttpRequest, String tcad, String tscId, boolean tracingEnabled) {
        super(null, contextHttpRequest, new HashMap<>(), 0);
        this.tid = tid;
        this.tcad = tcad;
        this.tscid = tscId;
        this.tracingEnabled = tracingEnabled;
    }

    public String getTid() {
        return tid;
    }

    public String getTcad() {
        return tcad;
    }

    public String getTscid() {
        return tscid;
    }

    public boolean isTracingEnabled() {
        return tracingEnabled;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("tid", tid)
                .append("tcad", tcad)
                .append("tscid", tscid)
                .toString();
    }
}
