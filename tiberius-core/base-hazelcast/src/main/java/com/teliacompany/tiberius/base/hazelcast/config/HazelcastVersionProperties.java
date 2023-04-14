package com.teliacompany.tiberius.base.hazelcast.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootVersion;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.SpringVersion;

@Configuration
@PropertySource(value = {"classpath:/versions/versions.hazelcast.properties"})
public class HazelcastVersionProperties {
    private static final Logger LOG = LoggerFactory.getLogger(HazelcastVersionProperties.class);

    private final String hazelcastVersion;
    private final String hazelcastSpringVersion;
    private final String hazelcastKubernetesVersion;

    public HazelcastVersionProperties(@Value("${hazelcast.version}") String hazelcastVersion,
                                      @Value("${hazelcast.spring.version}") String hazelcastSpringVersion,
                                      @Value("${hazelcast.kubernetes.version}") String hazelcastKubernetesVersion) {
        this.hazelcastVersion = hazelcastVersion;
        this.hazelcastSpringVersion = hazelcastSpringVersion;
        this.hazelcastKubernetesVersion = hazelcastKubernetesVersion;

        LOG.info("Hazelcast version: {}", hazelcastVersion);
        LOG.info("Hazelcast spring version: {}", hazelcastSpringVersion);
        LOG.info("Hazelcast k8s version: {}", hazelcastKubernetesVersion);
    }

    public String getHazelcastVersion() {
        return hazelcastVersion;
    }

    public String getHazelcastSpringVersion() {
        return hazelcastSpringVersion;
    }

    public String getHazelcastKubernetesVersion() {
        return hazelcastKubernetesVersion;
    }
}
