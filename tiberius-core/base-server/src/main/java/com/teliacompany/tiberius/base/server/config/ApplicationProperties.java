package com.teliacompany.tiberius.base.server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.ArrayList;
import java.util.List;

@Import({RegistrationProperties.class, AppBuildProperties.class, IntegrationUsageStatuses.class})
@Configuration
public class ApplicationProperties {
    private final IntegrationUsageStatuses integrationUsageStatuses;
    private final String applicationName;
    private final RegistrationProperties registrationProperties;
    private final List<String> activeSpringProfiles;
    private final Integer serverPort;
    private final String mainSpringProfile;
    private final AppBuildProperties buildProperties;
    private Integer wiremockPort;
    private String baseUriOverride;

    public ApplicationProperties(RegistrationProperties registrationProperties,
                                 AppBuildProperties buildProperties,
                                 IntegrationUsageStatuses integrationUsageStatuses,
                                 @Value("${spring.application.name:n/a}") String applicationName,
                                 @Value("#{T(java.util.Arrays).asList('${spring.profiles.active}')}") List<String> activeSpringProfiles,
                                 @Value("${server.port:8080}") Integer serverPort,
                                 @Value("${wiremock.port:8090}") Integer wiremockPort,
                                 @Value("${endpoint.baseuri.override:#{null}}") String baseUriOverride) {
        this.integrationUsageStatuses = integrationUsageStatuses;
        this.applicationName = applicationName;
        this.buildProperties = buildProperties;
        this.registrationProperties = registrationProperties;
        this.activeSpringProfiles = activeSpringProfiles == null ? new ArrayList<>() : activeSpringProfiles;
        this.mainSpringProfile = this.activeSpringProfiles.stream().findFirst().orElse("");
        this.serverPort = serverPort;
        this.wiremockPort = wiremockPort;
        this.baseUriOverride = baseUriOverride;
    }

    public RegistrationProperties getRegistrationProperties() {
        return registrationProperties;
    }

    public void setWiremockPort(Integer wireMockPort) {
        this.wiremockPort = wireMockPort;
    }

    public void setBaseUriOverride(String baseUriOverride) {
        this.baseUriOverride = baseUriOverride;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public List<String> getActiveSpringProfiles() {
        return activeSpringProfiles;
    }

    public Integer getWiremockPort() {
        return wiremockPort;
    }

    public String getBaseUriOverride() {
        return baseUriOverride;
    }

    public Integer getServerPort() {
        return serverPort;
    }

    public String getMainSpringProfile() {
        return mainSpringProfile;
    }

    public AppBuildProperties getBuildProperties() {
        return buildProperties;
    }

    public String getApplicationDescription() {
        return buildProperties.getDescription();
    }

    public String getArtifactId() {
        return buildProperties.getArtifactId();
    }

    public String getGroup() {
        return buildProperties.getGroup();
    }

    public IntegrationUsageStatuses getIntegrationUsageStatuses() {
        return integrationUsageStatuses;
    }
}
