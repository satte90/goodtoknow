package com.teliacompany.tiberius.base.server.api.smoketest;

import com.teliacompany.webflux.error.api.ErrorResponse;

public class SmokeTestSubServiceResult {
    private String name;
    private SmokeTestStatus status;
    private boolean validData;
    private long responseTime;
    private ErrorResponse error;

    public String getName() {
        return name;
    }

    public SmokeTestSubServiceResult setName(String name) {
        this.name = name;
        return this;
    }

    public SmokeTestStatus getStatus() {
        return status;
    }

    public SmokeTestSubServiceResult setStatus(SmokeTestStatus status) {
        this.status = status;
        return this;
    }

    public boolean isValidData() {
        return validData;
    }

    public SmokeTestSubServiceResult setValidData(boolean validData) {
        this.validData = validData;
        return this;
    }

    public long getResponseTime() {
        return responseTime;
    }

    public SmokeTestSubServiceResult setResponseTime(long responseTime) {
        this.responseTime = responseTime;
        return this;
    }

    public ErrorResponse getError() {
        return error;
    }

    public SmokeTestSubServiceResult setError(ErrorResponse error) {
        this.error = error;
        return this;
    }
}
