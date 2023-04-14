package com.teliacompany.webflux.request.processor.model;

public final class ProcessInternalRequestData {
    private final String tid;
    private final String tscId;
    private final String tcad;

    public ProcessInternalRequestData(String tid, String tcad, String tscId) {
        this.tid = tid;
        this.tscId = tscId;
        this.tcad = tcad;
    }

    public ProcessInternalRequestData(String tid, String tcad) {
        this(tid, tcad, null);
    }

    public ProcessInternalRequestData(String tid) {
        this(tid, null, null);
    }

    public String getTid() {
        return tid;
    }

    public String getTscId() {
        return tscId;
    }

    public String getTcad() {
        return tcad;
    }
}
