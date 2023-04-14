package com.teliacompany.tiberius.base.server.api;

public class TestModeData {
    private int wiremockPort;
    private Long timestamp;

    public void setWiremockPort(int wiremockPort) {
        this.wiremockPort = wiremockPort;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public int getWiremockPort() {
        return wiremockPort;
    }

    public Long getTimestamp() {
        return timestamp;
    }
}
