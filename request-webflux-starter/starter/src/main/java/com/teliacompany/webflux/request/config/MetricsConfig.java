package com.teliacompany.webflux.request.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
@ConfigurationProperties("metrics")
public class MetricsConfig {
    private static final Logger LOG = LoggerFactory.getLogger(MetricsConfig.class);

    private String prefix = null;

    @PostConstruct
    public void init() {
        if(prefix == null) {
            LOG.error("metrics prefix = null! No metrics will be reported");
        }

        LOG.info("*************************");
        LOG.info("Metrics config loaded:");
        LOG.info("metrics.prefix: {}", prefix);
        LOG.info("*************************");
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }


}
