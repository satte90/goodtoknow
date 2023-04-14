package com.teliacompany.webflux.request.status.model;

public class PingableService {
    private final String serviceName;
    private final String host;
    private final Integer port;

    private final String proxy;
    private final Integer proxyPort;

    private long responseTime;
    private String status;
    private String errorMessage;

    public PingableService(String serviceName, String host, Integer port, String proxy, Integer proxyPort) {
        this.serviceName = serviceName;
        this.host = host;
        this.port = port;
        this.proxy = proxy;
        this.proxyPort = proxyPort;
    }

    public String getServiceName() {
        return serviceName;
    }

    public long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(long responseTime) {
        this.responseTime = responseTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    public String getProxy() {
        return proxy;
    }

    public Integer getProxyPort() {
        return proxyPort;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
