package com.teliacompany.apigee4j.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;

import java.util.Arrays;

/**
 * This listener fires as soon as the spring environment is prepared. It will look the spring.active.profiles and pick the last one defined and set as
 * apigee4j.environment. This property can then be used normally, for example in @PropertySource annotations.
 */
public class ApigeeEnvironmentResolver implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(ApigeeEnvironmentResolver.class);
    private static final String apigee4j_ENVIRONMENT_KEY = "apigee4j.environment";

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        if(event.getEnvironment().getProperty(apigee4j_ENVIRONMENT_KEY) == null) {
            Arrays.stream(event.getEnvironment().getActiveProfiles())
                    .filter(profile -> !"local".equalsIgnoreCase(profile))
                    .reduce((first, second) -> second)
                    .ifPresent(lastActiveProfile -> System.setProperty(apigee4j_ENVIRONMENT_KEY, lastActiveProfile));
        }
        LOG.info("{} = {}", apigee4j_ENVIRONMENT_KEY, event.getEnvironment().getProperty(apigee4j_ENVIRONMENT_KEY));
    }
}

