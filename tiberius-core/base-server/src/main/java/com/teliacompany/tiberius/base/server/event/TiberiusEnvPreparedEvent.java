package com.teliacompany.tiberius.base.server.event;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePropertySource;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This listener fires as soon as the spring environment is prepared. It will look the spring.profiles.active and pick the last one defined and set as
 * spring.profiles.main. This property can then be used normally, for example in @PropertySource annotations.
 * Additionally, this filter will add all tiberius property sources for all profiles, last defined profile wins, i.e. if spring.profiles.active=dev,local
 * the property sources "tiberius.properties", "tiberius-dev.properties" and "tiberius-local.properties" will be loaded. Local will have the highest priority.
 * spring.profiles.main will be set to local.
 */
@Order(1)
public class TiberiusEnvPreparedEvent implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(TiberiusEnvPreparedEvent.class);
    private static final String SPRING_PROFILES_MAIN_KEY = "spring.profiles.main";
    private final ResourceLoader resourceLoader = new DefaultResourceLoader();

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        final ConfigurableEnvironment environment = event.getEnvironment();

        if(environment.getProperty(SPRING_PROFILES_MAIN_KEY) == null) {
            //Set some properties for logging
            String seconds = StringUtils.substring(String.valueOf(System.currentTimeMillis()), 0, -3);
            MDC.put("transactionId", "srt-" + seconds);
            MDC.put("tscid", "none");
            MDC.put("tcad", "none");

            // Get all profiles, filter out any "null" and null values and reset active profiles to the filtered list.
            List<String> profiles = Arrays.stream(environment.getActiveProfiles())
                    .filter(p -> p != null && !"null".equalsIgnoreCase(p))
                    .collect(Collectors.toList());

            // Then determine the main profile, i.e. the last specified profile. List of profiles needs to be reversed. If more than one profile is active
            // make sure local is not used as main profile. The main profile is used to look up endpoints e.t.c.
            Collections.reverse(profiles);

            if(profiles.size() == 1 && profiles.get(0).equalsIgnoreCase("local")) {
                LOG.warn("'local' cannot be the one and only profile, " +
                        "injecting 'dev' profile as well as this will mimic the old behaviour when dev was not used as a test environment. " +
                        "Add 'dev,' to your spring.profiles.active config");
                profiles.add(0, "dev");
                environment.addActiveProfile("dev");
            }

            profiles.stream()
                    .filter(p -> !"local".equalsIgnoreCase(p))
                    .findFirst()
                    .ifPresent(lastActiveProfile -> System.setProperty(SPRING_PROFILES_MAIN_KEY, lastActiveProfile));

            // Add tiberius property sources based on profiles in correct order. any application[-profile].properties will override properties in these files
            profiles.forEach(profile -> addPropertySourceIfItExist(environment, "tiberius-" + profile));
            addPropertySourceIfItExist(environment, "tiberius");
        }
        LOG.info("Active profiles: {}", Arrays.asList(environment.getActiveProfiles()));
        LOG.info("{} = {}", SPRING_PROFILES_MAIN_KEY, environment.getProperty(SPRING_PROFILES_MAIN_KEY));
    }

    private void addPropertySourceIfItExist(ConfigurableEnvironment environment, String propertySourceName) {
        String location = "classpath:/" + propertySourceName + ".properties";
        if(resourceLoader.getResource(location).exists()) {
            try {
                LOG.info("Adding property source {}: {}", propertySourceName, location);
                environment.getPropertySources().addLast(new ResourcePropertySource(propertySourceName, location));
            } catch(IOException e) {
                LOG.warn("Could not load property source {}", location);
            }
        }
    }
}

