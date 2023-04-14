package com.teliacompany.tiberius.base.server.api;

public class VersionsResponse {
    private String serviceVersion;
    private String tiberiusCoreVersion;
    private String requestWebfluxStarterVersion;
    private String errorWebfluxStarterVersion;
    private String jacksonWebfluxStarterVersion;
    private String springBootVersion;
    private String springVersion;
    private String log4jVersion;

    public String getServiceVersion() {
        return serviceVersion;
    }

    public VersionsResponse setServiceVersion(String serviceVersion) {
        this.serviceVersion = serviceVersion;
        return this;
    }

    public String getTiberiusCoreVersion() {
        return tiberiusCoreVersion;
    }

    public VersionsResponse setTiberiusCoreVersion(String tiberiusCoreVersion) {
        this.tiberiusCoreVersion = tiberiusCoreVersion;
        return this;
    }

    public String getRequestWebfluxStarterVersion() {
        return requestWebfluxStarterVersion;
    }

    public VersionsResponse setRequestWebfluxStarterVersion(String requestWebfluxStarterVersion) {
        this.requestWebfluxStarterVersion = requestWebfluxStarterVersion;
        return this;
    }

    public String getErrorWebfluxStarterVersion() {
        return errorWebfluxStarterVersion;
    }

    public VersionsResponse setErrorWebfluxStarterVersion(String errorWebfluxStarterVersion) {
        this.errorWebfluxStarterVersion = errorWebfluxStarterVersion;
        return this;
    }

    public String getSpringBootVersion() {
        return springBootVersion;
    }

    public VersionsResponse setSpringBootVersion(String springBootVersion) {
        this.springBootVersion = springBootVersion;
        return this;
    }

    public String getSpringVersion() {
        return springVersion;
    }

    public VersionsResponse setSpringVersion(String springVersion) {
        this.springVersion = springVersion;
        return this;
    }

    public String getJacksonWebfluxStarterVersion() {
        return jacksonWebfluxStarterVersion;
    }

    public VersionsResponse setJacksonWebfluxStarterVersion(String jacksonWebfluxStarterVersion) {
        this.jacksonWebfluxStarterVersion = jacksonWebfluxStarterVersion;
        return this;
    }

    public String getLog4jVersion() {
        return log4jVersion;
    }

    public VersionsResponse setLog4jVersion(String log4jVersion) {
        this.log4jVersion = log4jVersion;
        return this;
    }
}
