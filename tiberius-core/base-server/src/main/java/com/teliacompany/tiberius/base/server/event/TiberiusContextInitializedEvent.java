package com.teliacompany.tiberius.base.server.event;

import com.teliacompany.webflux.error.exception.server.InternalServerErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationContextInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.teliacompany.tiberius.base.server.config.BasePathConfiguration.TIBERIUS_PATH_PREFIX;

public class TiberiusContextInitializedEvent implements ApplicationListener<ApplicationContextInitializedEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(TiberiusContextInitializedEvent.class);

    @Override
    public void onApplicationEvent(ApplicationContextInitializedEvent event) {
        final ConfigurableEnvironment environment = event.getApplicationContext().getEnvironment();
        if(environment.getActiveProfiles().length == 0) {
            return;
        }

        final String applicationName = getApplicationName(environment);
        System.setProperty("application.name", applicationName);

        setBasePathProperty(environment, applicationName);
    }

    private String getApplicationName(ConfigurableEnvironment environment) {
        String appName = environment.getProperty("spring.application.name");
        if(appName == null) {
            String error = "\n\n!!!!!!!!!!!!!!!! ************** ************** !!!!!!!!!!!!!!!!\n" +
                    "Could not start app. applicationName not set!\n" +
                    "Set spring.application.name in your application.properties file!\n" +
                    "!!!!!!!!!!!!!!!! ************** ************** !!!!!!!!!!!!!!!!\n";
            LOG.error(error);

            throw new InternalServerErrorException(error);
        }
        return appName;
    }

    /**
     * if local or component test profile is active set TIBERIUS_PATH_PREFIX to automatically determined value based on app name in a property source with
     * low priority, i.e. can be overridden
     * For other profiles set it to empty string.
     *
     * @param environment     - environment
     * @param applicationName - name of application, e.g. tiberius-address
     */
    private void setBasePathProperty(ConfigurableEnvironment environment, String applicationName) {
        String path;
        List<String> profiles = Arrays.asList(environment.getActiveProfiles());
        // For test and local profile, tiberiusPathPrefix will be "automatic", otherwise empty string. (if not set specifically in properties)
        final String tiberiusPathPrefix = environment.getProperty("tiberius.path.prefix", getDefaultValue(profiles));
        if(tiberiusPathPrefix.equals("automatic")) {
            // LOCAL, COMPONENTTEST
            path = applicationName.replace("-", "/");
        } else {
            // DEV, SIT, AT, BETA, PROD, path will (if not set specifically) be empty string.
            // So the property "tiberius.path.prefix" will be set to empty string. Which is what we want for mentioned environments.
            path = tiberiusPathPrefix;
        }
        Map<String, Object> props = Map.of(TIBERIUS_PATH_PREFIX, path);
        PropertySource<?> ps = new MapPropertySource("TiberiusPathPrefix", props);
        environment.getPropertySources().addLast(ps);
        LOG.info("{} = {}", TIBERIUS_PATH_PREFIX, environment.getProperty(TIBERIUS_PATH_PREFIX));
    }

    private static String getDefaultValue(List<String> profiles) {
        return profiles.contains("local") || profiles.contains("componenttest") ? "automatic" : "";
    }
}
