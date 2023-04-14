package com.teliacompany.tiberius.base.toca.app;

import com.teliacompany.tiberius.base.utils.TiberiusEventOrder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePropertySource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This listener fires as soon as the spring environment is prepared.
 * This filter will add all toca property sources for all profiles, last defined profile wins, i.e. if spring.profiles.active=dev,local
 * the property sources "tiberius.properties", "tiberius-dev.properties" and "tiberius-local.properties" will be loaded.
 * In this example case Local properties will override properties with the same name in both toca.properties and toca-dev.properties.
 * <p>
 * toca-local.properties will always be processed last (overrides others) if local profile is present
 *
 * Needs a higher number in @Order annotation than TiberiusEnvironmentPrepared in base-server to make sure this is fired after
 */
@SuppressWarnings("DuplicatedCode")
@Order(TiberiusEventOrder.TOCA_ENVIRONMENT_PREPARED)
public class TocaEnvPreparedEvent implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(TocaEnvPreparedEvent.class);
    private static final ResourceLoader RESOURCE_LOADER = new DefaultResourceLoader();
    private static String TIBERIUS_PROPERTY_SOURCE_NAME = "tiberius";

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        final ConfigurableEnvironment environment = event.getEnvironment();
        // Get the main profile, should be set by TiberiusEnvironmentPrepared - the @Order annotation controls in what order events are fired
        final String mainProfile = environment.getProperty("spring.profiles.main", "");
        final boolean isLocal = Arrays.stream(environment.getActiveProfiles())
                .anyMatch("local"::equalsIgnoreCase);

        TIBERIUS_PROPERTY_SOURCE_NAME = getHighestPriorityTiberiusPropertySourceName(isLocal, mainProfile);

        addTocaPropertySources(environment);
        addTocaAutomaticProperties(environment);
    }

    private static String getHighestPriorityTiberiusPropertySourceName(boolean isLocal, String mainProfile) {
        if(isLocal) {
            return "tiberius-local";
        }
        if("componenttest".equals(mainProfile)) {
            return "tiberius";
        }
        return StringUtils.isEmpty(mainProfile) ? "tiberius" : "tiberius-" + mainProfile;
    }

    private static void addTocaPropertySources(ConfigurableEnvironment environment) {
        // Get all profiles, filter out any "null" and null values and reset active profiles to the filtered list.
        List<String> profiles = Arrays.stream(environment.getActiveProfiles())
                .filter(p -> p != null && !"null".equalsIgnoreCase(p))
                .collect(Collectors.toList());

        if(profiles.contains("local")) {
            //If local profile is present, make sure it is processed last
            profiles.remove("local");
            profiles.add("local");
        }

        // Then determine the main profile, i.e. the last specified profile. List of profiles needs to be reversed. If more than one profile is active
        // make sure local is not used as main profile. The main profile is used to look up endpoints e.t.c.
        Collections.reverse(profiles);

        // Add tiberius property sources based on profiles in correct order. any application[-profile].properties will override properties in these files
        addFilePropertySourceIfItExist(environment, "toca", TIBERIUS_PROPERTY_SOURCE_NAME);
        profiles.forEach(profile -> addFilePropertySourceIfItExist(environment, "toca-" + profile, "toca"));
    }

    private static void addFilePropertySourceIfItExist(ConfigurableEnvironment environment, String propertySourceName, String addBeforePropertySourceWithThisName) {
        String location = "classpath:/" + propertySourceName + ".properties";
        if(RESOURCE_LOADER.getResource(location).exists()) {
            try {
                LOG.info("Adding property source {}: {}", propertySourceName, location);
                environment.getPropertySources().addBefore(addBeforePropertySourceWithThisName, new ResourcePropertySource(propertySourceName, location));
            } catch(IOException e) {
                LOG.warn("Could not load property source {}", location);
            }
        }
    }

    /**
     * Adds default properties for toca application.
     * These are added with low (the lowest even?) priority so any property in toca or application property file will override
     */
    private static void addTocaAutomaticProperties(ConfigurableEnvironment environment) {
        //Automatic Toca properties
        final String appName = environment.getProperty("spring.application.name", "");
        final String tiberiusPathPrefix = environment.getProperty("tiberius.path.prefix", "");

        // Calculate path prefix
        String prefix;
        if(StringUtils.isEmpty(tiberiusPathPrefix)) {
            prefix = StringUtils.removeStart(appName, "tiberius-toca-");
            prefix = StringUtils.replace(prefix, "-", "");
        } else {
            prefix = tiberiusPathPrefix;
        }
        final String tocaPathPrefix = StringUtils.removeEnd(prefix, "/");

        String loggingIgnorePathsCsv = Arrays.stream(environment.getProperty("logging.ignorePaths", "").split(","))
                .map(m -> "/" + tocaPathPrefix + "/" + StringUtils.removeStart(m, "/"))
                .collect(Collectors.joining(","));

        Map<String, String> automaticProperties = new HashMap<>();
        automaticProperties.put("tiberius.path.prefix", tocaPathPrefix);
        automaticProperties.put("logging.ignorePaths", loggingIgnorePathsCsv);
        addMapPropertySource(environment, "toca-automatic", automaticProperties);
    }

    @SuppressWarnings("SameParameterValue")
    private static void addMapPropertySource(ConfigurableEnvironment environment, String name, Map<String, String> properties) {
        final String propString = properties.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("\n"));
        final InputStream inStream = new ByteArrayInputStream(propString.getBytes(StandardCharsets.UTF_8));
        final Resource resource = new InputStreamResource(inStream);
        try {
            environment.getPropertySources().addBefore(TIBERIUS_PROPERTY_SOURCE_NAME, new ResourcePropertySource(name, resource));
        } catch(IOException e) {
            LOG.error("Could not add property source with name {} from map due to: ", name, e);
        }
    }
}

