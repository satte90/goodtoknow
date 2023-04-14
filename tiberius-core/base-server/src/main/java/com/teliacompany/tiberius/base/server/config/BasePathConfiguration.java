package com.teliacompany.tiberius.base.server.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.reactive.config.PathMatchConfigurer;
import org.springframework.web.reactive.config.WebFluxConfigurer;

/**
 * Add tiberius/app-name to rest api to mimic address in test and prod environments
 */
@Configuration
public class BasePathConfiguration implements WebFluxConfigurer {
    private static final Logger LOG = LoggerFactory.getLogger(BasePathConfiguration.class);

    public static final String TIBERIUS_PATH_PREFIX = "tiberius.path.prefix";

    private final String basePath;

    public BasePathConfiguration(@Value("${tiberius.path.prefix}") String basePath) {
        this.basePath = basePath;
    }

    @Override
    public void configurePathMatching(PathMatchConfigurer configurer) {
        LOG.info("Setting base path to: {}", basePath);
        configurer.addPathPrefix(basePath, clazz -> true);
    }
}
