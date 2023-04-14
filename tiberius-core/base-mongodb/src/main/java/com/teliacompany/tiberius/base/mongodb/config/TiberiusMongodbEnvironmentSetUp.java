package com.teliacompany.tiberius.base.mongodb.config;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.lang.NonNull;

import java.util.Map;

import static org.springframework.core.env.StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME;

@Order(10)
public class TiberiusMongodbEnvironmentSetUp implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(TiberiusMongodbEnvironmentSetUp.class);

    private static final String PROPERTY_APPLICATION_NAME = "spring.application.name";
    private static final String PROPERTY_SPRING_PROFILE_MAIN = "spring.profiles.main";
    private static final String PROPERTY_USER = "tiberius.mongodb.user";
    private static final String PROPERTY_PASSWORD = "tiberius.mongodb.password";
    private static final String PROPERTY_HOST = "tiberius.mongodb.host";
    private static final String PROPERTY_PORT = "tiberius.mongodb.port";
    private static final String PROPERTY_DATABASE = "tiberius.mongodb.database";

    private static final String NOT_AVAILABLE = "N/A";
    private static final String DEFAULT_HOST = "tiberius-mongodb.tse.svc.cluster.local";
    private static final String DEFAULT_PORT = "27017";

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent applicationEnvironmentPreparedEvent) {
        // tiberius-mongodb.tse.svc.cluster.local
        // databaseName = tiberius-praefectus or tiberius-products-at
        ConfigurableEnvironment environment = applicationEnvironmentPreparedEvent.getEnvironment();

        String databaseUserName = getDatabaseUser(environment);
        String tiberiusMongoDbHost = getTiberiusProperty(environment, PROPERTY_HOST, DEFAULT_HOST);
        String tiberiusMongoDbPort = getTiberiusProperty(environment, PROPERTY_PORT, DEFAULT_PORT);
        String databaseName = getDatabaseName(environment, databaseUserName);
        String mongodbUri;
        if(tiberiusMongoDbHost.equals("localhost")) {
            LOG.info("Setting spring.data.mongodb.uri to: mongodb://{}:{}/{}", tiberiusMongoDbHost, tiberiusMongoDbPort, databaseName);
            mongodbUri = String.format("mongodb://%s:%s/%s", tiberiusMongoDbHost, tiberiusMongoDbPort, databaseName);
        } else {
            LOG.info("Setting spring.data.mongodb.uri to: mongodb://{}:********@{}:{}/{}", databaseUserName, tiberiusMongoDbHost, tiberiusMongoDbPort, databaseName);
            String applicationPassword = getDatabasePassword(environment, databaseUserName);
            mongodbUri = String.format("mongodb://%s:%s@%s:%s/%s", databaseUserName, applicationPassword, tiberiusMongoDbHost, tiberiusMongoDbPort, databaseName);
        }
        Map<String, Object> mongodbProperties = Map.of("spring.data.mongodb.uri", mongodbUri);
        environment.getPropertySources()
                .addAfter(SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, new MapPropertySource("tiberiusMongoDb", mongodbProperties));
    }

    /**
     * Get tiberius mongodb property from the correct source
     * Filter out any invalid properties, for some reason something is setting tiberius.mongodb.port property in configurationProperties and/or systemEnvironment to
     * tcp://xxx.xx.xx.xxx:27017 (x is [0-9]). We don't want these. We want it to come from application[-env].properties or tiberius[-env].properties.
     */
    private String getTiberiusProperty(ConfigurableEnvironment environment, String propertyName, String defaultValue) {
        return environment.getPropertySources()
                .stream()
                .filter(ps -> !"configurationProperties".equals(ps.getName()) && !"systemEnvironment".equals(ps.getName()))
                .filter(ps -> ps.containsProperty(propertyName))
                .map(ps -> (String) ps.getProperty(propertyName))
                .findFirst()
                .orElse(defaultValue);
    }

    private String getDatabaseUser(ConfigurableEnvironment environment) {
        return getTiberiusProperty(environment, PROPERTY_USER, environment.getProperty(PROPERTY_APPLICATION_NAME, NOT_AVAILABLE));
    }

    private String getDatabasePassword(ConfigurableEnvironment environment, @NonNull String databaseUserName) {
        String password = getTiberiusProperty(environment, PROPERTY_PASSWORD, null);
        if(password == null) {
            String[] tmp = databaseUserName.split("-");
            StringBuilder sb = new StringBuilder();
            for(int i = tmp.length - 1; i >= 0; i--) {
                sb.append(tmp[i]).append("-");
            }
            return StringUtils.removeEnd(sb.toString(), "-");
        }
        return password;
    }

    private String getDatabaseName(ConfigurableEnvironment environment, String databaseUserName) {
        String dbName = getTiberiusProperty(environment, PROPERTY_DATABASE, null);
        if(dbName == null) {
            String dbNameSuffix = "";
            String mainEnvProfile = environment.getProperty(PROPERTY_SPRING_PROFILE_MAIN, "local");
            if(!mainEnvProfile.equals("prod") && !mainEnvProfile.equals("local")) {
                dbNameSuffix = "-" + mainEnvProfile;
            }
            return databaseUserName + dbNameSuffix;
        }
        return dbName;
    }
}
