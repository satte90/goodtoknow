package com.teliacompany.tiberius.base.server.config.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
public class DefaultTiberiusSecurityConfigurer implements TiberiusSecurityConfigurer {
    private static final List<String> TIBERIUS_OPEN_ENDPOINTS = Arrays.asList(
            "testsupport/**",
            "methods",
            "devops/**",
            "swagger"
    );
    private final String apiDocsPath;

    public DefaultTiberiusSecurityConfigurer(@Value("${springdoc.api-docs.path}") String apiDocsPath) {
        this.apiDocsPath = apiDocsPath;
    }

    @Override
    public List<String> additionalUnsecuredEndpoints() {
        final List<String> allOpenEndpoints = new ArrayList<>(TIBERIUS_OPEN_ENDPOINTS);
        allOpenEndpoints.add(apiDocsPath);
        return allOpenEndpoints;
    }
}
