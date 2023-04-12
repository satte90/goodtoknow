package com.teliacompany.apigee4j.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import javax.annotation.PostConstruct;

/**
 * Configuration for apigee.
 * Default configuration can be found in: apigee4j.properties
 * Default profile specific config found in apigee4j-${apigee4j.environment}.properties
 * Config can be overridden and extended in normal application.properties / application-${apigee4j.environment}.properties
 * <p>
 * NB!
 * Credentials (apigee.key & apigee.secret) has to be defined in each individual consumer (I.e. YOUR SERVICE) for all but dev environment.
 * Dev is configured by default to localhost:8089 where a mocking service (e.g. wiremock) should be running. You can of course configure this as you want in your
 * application.properties file.
 */
@Configuration
@PropertySource(value = {
        "classpath:/apigee4j.properties",
        "classpath:/apigee4j-${apigee4j.environment}.properties"
}, ignoreResourceNotFound = true)
public class ApigeeConnectionConfig {
    private static final Logger LOG = LoggerFactory.getLogger(ApigeeConnectionConfig.class);

    @Value("${apigee.host}")
    public String apigeeHost;

    @Value("${apigee.refreshEndpoint}")
    public String refreshEndpoint;

    @Value("${apigee.key}")
    public String key;

    @Value("${apigee.secret}")
    public String secret;

    @Value("${apigee.proxy.host:}")
    public String proxyHost;
    @Value("${apigee.proxy.port:0}")
    public Integer proxyPort;
    @Value("${apigee.proxy.enabled:false}")
    public boolean proxyEnabled;

    @PostConstruct
    public void init() {
        LOG.info("Loaded apigee config");
        LOG.info("Apigee refreshUrl = {}{}", apigeeHost, refreshEndpoint);
        LOG.info("Apigee key = {}", key);
        if(secret != null && !secret.isEmpty()) {
            LOG.info("Apigee secret = <secret>");
        } else {
            LOG.warn("Apigee secret is not set!");
        }

        LOG.info("Apigee proxy enabled = {}", proxyEnabled);
        LOG.info("Apigee proxy host = {}", proxyHost);
        LOG.info("Apigee proxy port = {}", proxyPort);

    }

    public String getRefreshUrl() {
        return apigeeHost + refreshEndpoint;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public Integer getProxyPort() {
        return proxyPort;
    }

    public boolean isProxyEnabled() {
        return proxyEnabled;
    }
}
