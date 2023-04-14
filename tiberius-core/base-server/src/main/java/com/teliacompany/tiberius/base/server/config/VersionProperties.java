package com.teliacompany.tiberius.base.server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.SpringVersion;

@Configuration
@PropertySource(value = {"classpath:/versions/versions.properties", "classpath:/META-INF/build-info.properties"})
public class VersionProperties {
    private final String appVersion;
    private final String tiberiusCoreVersion;
    private final String requestWebfluxStarterVersion;
    private final String errorWebfluxStarterVersion;
    private final String log4jVersion;
    private final String jacksonWebfluxStarterVersion;
    private final String springBootVersion;
    private final String springVersion;

    public VersionProperties(@Value("${build.version}") String buildVersion,
                             @Value("${tiberius.core.version}") String tiberiusCoreVersion,
                             @Value("${tiberius.dependency.request.webflux.starter.version}") String requestWebfluxStarterVersion,
                             @Value("${tiberius.dependency.jackson.webflux.starter.version}") String jacksonWebfluxStarterVersion,
                             @Value("${tiberius.dependency.error.webflux.starter.version}") String errorWebfluxStarterVersion,
                             @Value("${tiberius.dependency.log4j.version}") String log4jVersion) {
        this.appVersion = buildVersion;
        this.tiberiusCoreVersion = tiberiusCoreVersion;
        this.requestWebfluxStarterVersion = requestWebfluxStarterVersion;
        this.jacksonWebfluxStarterVersion = jacksonWebfluxStarterVersion;
        this.errorWebfluxStarterVersion = errorWebfluxStarterVersion;
        this.log4jVersion = log4jVersion;
        this.springVersion = SpringVersion.getVersion();
        this.springBootVersion = SpringBootVersion.getVersion();
    }

    public String getAppVersion() {
        return appVersion;
    }

    public String getTiberiusCoreVersion() {
        return tiberiusCoreVersion;
    }

    public String getRequestWebfluxStarterVersion() {
        return requestWebfluxStarterVersion;
    }

    public String getErrorWebfluxStarterVersion() {
        return errorWebfluxStarterVersion;
    }

    public String getSpringBootVersion() {
        return springBootVersion;
    }

    public String getSpringVersion() {
        return springVersion;
    }

    public String getJacksonWebfluxStarterVersion() {
        return jacksonWebfluxStarterVersion;
    }

    public String getLog4jVersion() {
        return log4jVersion;
    }
}
