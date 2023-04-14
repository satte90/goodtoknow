package com.teliacompany.tiberius.base.server;

import com.teliacompany.tiberius.base.server.exception.MissingTiberiusAnnotationException;
import com.teliacompany.tiberius.base.utils.ClassNameUtils;
import org.springframework.boot.Banner.Mode;
import org.springframework.boot.builder.SpringApplicationBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * This is used by Tiberius applications to start themselves with default properties and settings
 */
public final class TiberiusRunner {
    public static final String LOCAL = "local";

    public static void run(Class<?> clazz, String[] args) {
        final TiberiusApplication annotation = getTiberiusApplicationAnnotation(clazz);
        final String applicationName = getApplicationName(clazz, annotation);

        boolean isSpringEnvLocal = determineIfLocalRuntime();

        // Turn the args array into a list that we can potentially modify later
        List<String> argsList = new ArrayList<>(Arrays.asList(args));

        List<String> profiles = getProfilesFromCommandLine(argsList);

        // Add local as spring profile if env variable spring.environment.local is true. Local profile should be last
        if(isSpringEnvLocal && !profiles.contains(LOCAL)) {
            profiles.add(LOCAL);
        }

        final String[] profilesArray = profiles.toArray(new String[0]);
        final boolean isLocalProfilePresent = profiles.contains(LOCAL);
        final Properties defaultProperties = createDefaultProperties(applicationName, profilesArray, isLocalProfilePresent);

        // Remove spring.profiles.active commandLineArgument if it exist, then if it was removed re-add it with profiles (potentially with local)
        // The command line argument is needed as it has highest priority (over the default application.properties; which would set it to dev,local)
        if(argsList.removeIf(a -> a.startsWith("--spring.profiles.active"))) {
            argsList.add("--spring.profiles.active=" + String.join(",", profiles));
        }

        new SpringApplicationBuilder(clazz)
                .logStartupInfo(false)
                .bannerMode(Mode.OFF)
                .properties(defaultProperties)
                .profiles(profilesArray)
                .run(argsList.toArray(new String[0]));
    }

    private static TiberiusApplication getTiberiusApplicationAnnotation(Class<?> clazz) {
        TiberiusApplication annotation = clazz.getAnnotation(TiberiusApplication.class);
        if(annotation == null) {
            throw new MissingTiberiusAnnotationException();
        }
        return annotation;
    }

    private static String getApplicationName(Class<?> clazz, TiberiusApplication annotation) {
        String applicationName = annotation.applicationName();
        if(TiberiusApplication.AUTOMATIC_APPLICATION_NAME.equals(applicationName)) {
            applicationName = ClassNameUtils.simpleNameToKebabCase(clazz);
        }
        return applicationName;
    }

    /**
     * This property is set by spring-boot-maven-plugin. It is set to true if the maven profile local is used (mvn spring-boot:run -Plocal or -Pdev,local)
     */
    private static boolean determineIfLocalRuntime() {
        return "true".equalsIgnoreCase(System.getenv().getOrDefault("spring.environment.local", "false"));
    }

    /**
     * Extracts the profiles provided in command line argument: --spring.profiles.active
     * If command line argument is not found return empty list
     */
    private static List<String> getProfilesFromCommandLine(List<String> argsList) {
        return argsList.stream()
                .filter(a -> a.startsWith("--spring.profiles.active"))
                .findFirst()
                .map(arg -> arg.split("=").length == 2 ? arg.split("=")[1] : ",")
                .map(profileCsv -> profileCsv.split(","))
                .map(array -> new ArrayList<>(Arrays.asList(array)))
                .orElse(new ArrayList<>());
    }


    /**
     * Add default properties, this is added to the default property source which is overridden by all other property sources for example:
     * application.properties, application-env.properties, tiberius.properties, commandLine arguments, systemProperties, environmentProperties
     */
    private static Properties createDefaultProperties(String applicationName, String[] profilesArray, boolean isLocalProfilePresent) {
        Properties defaultProperties = new Properties();
        defaultProperties.put("logging.level.org.springframework.context.annotation.AutoProxyRegistrar", "WARN");
        defaultProperties.put("logging.level.org.springframework.cloud.context.scope", "WARN");
        defaultProperties.put("spring.main.log-startup-info", false);
        defaultProperties.put("logging.config", isLocalProfilePresent ? "classpath:log4j2-local.xml" : "classpath:log4j2.xml");
        defaultProperties.put("spring.application.name", applicationName);
        defaultProperties.put("spring.profiles.active", String.join(",", profilesArray));
        return defaultProperties;
    }
}
