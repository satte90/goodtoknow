package com.teliacompany.tiberius.base.server.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    private static final String AUTOMATIC = "automatic";

    private final String springDocBasePath;
    private final String serverPath;
    private final String apiDocs;
    private final String swaggerStaticPath;

    public SwaggerConfig(ApplicationProperties applicationProperties,
                         @Value("${tiberius.path.prefix}") String tiberiusPathPrefix,
                         @Value("${springdoc.base.path:automatic}") String springDocBasePath,
                         @Value("${springdoc.api-docs.path}") String apiDocs,
                         @Value("${springdoc.openapi.server.path:automatic}") String openApiServerPath,
                         @Value("${tiberius.swagger.static.path}") String swaggerStaticPath) {
        this.apiDocs = apiDocs;
        this.swaggerStaticPath = swaggerStaticPath;
        if(AUTOMATIC.equals(springDocBasePath)) {
            final String appName = applicationProperties.getApplicationName();
            // Generally tiberiusPathPrefix will be empty string for test and prod environments, set by TiberiusContextInitialized class.
            // For swagger we use appPath as the serverPath. In test adn prod environments the tiberiusPathPrefix will be empty but the service STILL has
            // a base path, but it is controlled by the infrastructure (see chart files) rather than spring.
            final String appPath = StringUtils.isEmpty(tiberiusPathPrefix)
                    ? StringUtils.replace(appName, "-", "/")
                    : tiberiusPathPrefix;

            if(applicationProperties.getActiveSpringProfiles().contains("local")) {
                this.springDocBasePath = String.format("/%s", appPath);
                this.serverPath = AUTOMATIC.equals(openApiServerPath) ? "/" : openApiServerPath;
            } else {
                // Try to generate springDoc base path (Note different from swagger base path!)
                // tiberius-subscription = /env/tiberius/subscription
                // tiberius-princeps-primis = /env/tiberius-princeps/primis
                this.springDocBasePath = String.format("/%s/%s", applicationProperties.getMainSpringProfile(), appPath);
                this.serverPath = AUTOMATIC.equals(openApiServerPath) ? this.springDocBasePath : openApiServerPath;
            }
        } else {
            // For cases where the springDocBasePath cannot be generated based on application name for whatever reason. The tiberius service itself can set this property
            // to force set the base path for springDoc/swagger server
            this.springDocBasePath = springDocBasePath;
            this.serverPath = AUTOMATIC.equals(openApiServerPath) ? springDocBasePath : openApiServerPath;
        }

    }

    public String getSpringDocBasePath() {
        return springDocBasePath;
    }

    public String getApiDocs() {
        return apiDocs;
    }

    public String getSwaggerStaticPath() {
        return swaggerStaticPath;
    }

    public String getServerPath() {
        return serverPath;
    }
}
