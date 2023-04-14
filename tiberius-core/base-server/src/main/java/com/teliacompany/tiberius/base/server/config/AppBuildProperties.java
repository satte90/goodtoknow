package com.teliacompany.tiberius.base.server.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;

@Configuration
public class AppBuildProperties {
    private final String artifactId;
    private final String group;
    private final Instant buildTime;
    private final String description;

    public AppBuildProperties(@Value("${build.artifact}") String artifactId,
                              @Value("${build.group}") String group,
                              @Value("${build.time}") String buildTime,
                              @Value("${build.description}") String description) {
        this.artifactId = artifactId;
        this.group = group;
        this.buildTime = parseBuildTime(buildTime);
        this.description = description;
    }

    private Instant parseBuildTime(String buildTime) {
        if(buildTime == null) {
            return null;
        }
        return Instant.parse(StringUtils.replace(buildTime, "\\", ""));
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getGroup() {
        return group;
    }

    public Instant getBuildTime() {
        return buildTime;
    }

    public String getDescription() {
        return description;
    }
}
