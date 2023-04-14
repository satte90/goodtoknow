package com.teliacompany.tiberius.base.server.api.smoketest;

public enum SmokeTestStatus {
    OK(0), SLOW(1), FAILED(2), NOT_CHECKED(-1);

    private final int code;

    SmokeTestStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
