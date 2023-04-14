package com.teliacompany.tiberius.base.server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SlackConfig {
    private final String host;
    private final String basePath;
    private final String defaultEndpoint;
    private final String serviceStatusEndpoint;
    private final String tibtestEndpoint;
    private final boolean proxyEnabled;
    private final String proxyHost;
    private final int proxyPort;
    private final boolean enabled;

    public SlackConfig(@Value("${tiberius.slack.devops.host:localhost}") String host,
                       @Value("${tiberius.slack.devops.base.endpoint:na}") String basePath,
                       @Value("${tiberius.slack.devops.default.endpoint:na}") String defaultEndpoint,
                       @Value("${tiberius.slack.devops.servicestatus.endpoint:na}") String serviceStatusEndpoint,
                       @Value("${tiberius.slack.devops.tibtest.endpoint:na}") String tibtestEndpoint,
                       @Value("${tiberius.slack.devops.proxy.enabled:true}") boolean proxyEnabled,
                       @Value("${tiberius.slack.devops.proxy.host:proxy-se.ddc.teliasonera.net}") String proxyHost,
                       @Value("${tiberius.slack.devops.proxy.port:8080}") int proxyPort,
                       @Value("${tiberius.slack.devops.enabled:false}") boolean enabled) {
        this.host = host;
        this.basePath = basePath;
        this.defaultEndpoint = defaultEndpoint;
        this.tibtestEndpoint = tibtestEndpoint;
        this.proxyEnabled = proxyEnabled;
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.enabled = enabled;
        this.serviceStatusEndpoint = serviceStatusEndpoint;
    }

    public String getHost() {
        return host;
    }

    public String getBasePath() {
        return basePath;
    }

    public String getDefaultEndpoint() {
        return defaultEndpoint;
    }

    public String getServiceStatusEndpoint() {
        return serviceStatusEndpoint;
    }

    public String getTibtestEndpoint() {
        return tibtestEndpoint;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public boolean isProxyEnabled() {
        return proxyEnabled;
    }
}
