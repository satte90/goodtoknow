package com.teliacompany.tiberius.base.server.api;

import java.time.Instant;
import java.util.List;
import java.util.StringJoiner;

public class ServiceRegistryEntry {
    private String serviceName;
    private String hostName;
    private String ip;
    private Integer port;
    private Long timestamp;
    private String environment; //Prod, sit, at, dev, local
    private VersionsResponse versions;
    private List<String> integrations; //Names of integrations found in WebClients
    private Instant buildTime;

    public String getServiceName() {
        return serviceName;
    }

    public ServiceRegistryEntry setServiceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

    public String getHostName() {
        return hostName;
    }

    public ServiceRegistryEntry setHostName(String hostName) {
        this.hostName = hostName;
        return this;
    }

    public String getIp() {
        return ip;
    }

    public ServiceRegistryEntry setIp(String ip) {
        this.ip = ip;
        return this;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public ServiceRegistryEntry setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public String getEnvironment() {
        return environment;
    }

    public ServiceRegistryEntry setEnvironment(String environment) {
        this.environment = environment;
        return this;
    }

    public Integer getPort() {
        return port;
    }

    public ServiceRegistryEntry setPort(Integer port) {
        this.port = port;
        return this;
    }

    public VersionsResponse getVersions() {
        return versions;
    }

    public ServiceRegistryEntry setVersions(VersionsResponse versions) {
        this.versions = versions;
        return this;
    }

    public ServiceRegistryEntry setIntegrations(List<String> integrations) {
        this.integrations = integrations;
        return this;
    }

    public List<String> getIntegrations() {
        return integrations;
    }

    public ServiceRegistryEntry setBuildTime(Instant buildTime) {
        this.buildTime = buildTime;
        return this;
    }

    public Instant getBuildTime() {
        return buildTime;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ServiceRegistryEntry.class.getSimpleName() + "[", "]")
                .add("serviceName='" + serviceName + "'")
                .add("hostName='" + hostName + "'")
                .add("ip='" + ip + "'")
                .add("port=" + port)
                .add("timestamp=" + timestamp)
                .add("environment='" + environment + "'")
                .add("versions=" + versions)
                .add("integrations=" + integrations)
                .add("buildTime=" + buildTime)
                .toString();
    }
}
