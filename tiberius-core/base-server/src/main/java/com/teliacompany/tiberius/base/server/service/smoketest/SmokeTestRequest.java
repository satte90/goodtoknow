package com.teliacompany.tiberius.base.server.service.smoketest;

public class SmokeTestRequest {
    private final String appName;

    public SmokeTestRequest(String appName) {
        this.appName = appName;
    }

    public String getAppName() {
        return appName;
    }
}
