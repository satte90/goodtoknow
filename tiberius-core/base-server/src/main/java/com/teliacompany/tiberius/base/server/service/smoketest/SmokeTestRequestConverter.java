package com.teliacompany.tiberius.base.server.service.smoketest;

public final class SmokeTestRequestConverter {
    private SmokeTestRequestConverter() {
        //Not to be instantiated
    }
     public static SmokeTestRequest convert(String appName) {
        return new SmokeTestRequest(appName);
     }
}
