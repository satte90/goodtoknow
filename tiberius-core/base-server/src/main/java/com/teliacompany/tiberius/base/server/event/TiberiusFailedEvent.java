package com.teliacompany.tiberius.base.server.event;

import com.teliacompany.tiberius.base.server.integration.slack.SlackPanicClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

public class TiberiusFailedEvent implements ApplicationListener<ApplicationFailedEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(TiberiusFailedEvent.class);
    private boolean applicationFailedEventRegistered = false;

    @Override
    public void onApplicationEvent(ApplicationFailedEvent applicationFailedEvent) {
        if(!applicationFailedEventRegistered) {
            applicationFailedEventRegistered = true;
            String profiles;
            String applicationName;
            if(applicationFailedEvent.getApplicationContext() != null) {
                ConfigurableEnvironment env = getEnvironment(applicationFailedEvent);
                profiles = env.getProperty("spring.profiles.active", "");
                applicationName = env.getProperty("spring.application.name", applicationFailedEvent.getSpringApplication().getMainApplicationClass().getSimpleName());
            } else {
                profiles = System.getProperty("spring.profiles.active");
                applicationName = applicationFailedEvent.getSpringApplication().getMainApplicationClass().getSimpleName();
                String regex = "([a-z])([A-Z]+)";
                String replacement = "$1-$2";
                applicationName = applicationName.replaceAll(regex, replacement).toLowerCase();
            }

            String envProfile = Arrays.stream(profiles.toLowerCase(Locale.ROOT).split(","))
                    .filter(e -> !e.equals("local"))
                    .filter(e -> !e.equals("prod"))
                    .findFirst()
                    .map(e -> "-" + e)
                    .orElse("");

            final Throwable exception = applicationFailedEvent.getException();
            final Throwable cause = exception.getCause();
            LOG.info("Application {} Failed... {}", applicationName, exception.getMessage(), exception);

            //Try to dig up the logfile and send it in mattermost so we get it before Kubernetes takes down the pod
            String serviceLog = null;
            File[] directories = new File("/opt/apps/logs/kubernetes/tse/deployments/" + applicationName + envProfile).listFiles(File::isDirectory);
            if(directories != null && directories.length > 0) {
                Path filePath = Paths.get(directories[0].getPath(), "service.log");
                try {
                    serviceLog = String.format("Full log:\n%s", Files.readString(filePath));
                } catch(IOException e) {
                    LOG.warn("Could not read logfile...");
                }
            }

            //If log file could not be read take stacktrace from error instead
            if(serviceLog == null) {
                String stacktraceMessage = Arrays.stream(exception.getStackTrace())
                        .map(StackTraceElement::toString)
                        .collect(Collectors.joining("\n    "));

                String causeStacktraceMessage = Arrays.stream(cause.getStackTrace())
                        .map(StackTraceElement::toString)
                        .collect(Collectors.joining("\n    "));

                serviceLog = "\n" + stacktraceMessage + "\n\n*Caused by:*\n" + causeStacktraceMessage + "\n\n";
            }

            String message = "Failed to start " + applicationName + " due to: " + exception.getMessage() + " \n\n" + cause.getMessage() + " \n\n" + serviceLog;
            SlackPanicClient.postSlackPanicMessage(getEnvironment(applicationFailedEvent), applicationName, message);
        }
    }

    private ConfigurableEnvironment getEnvironment(ApplicationFailedEvent applicationFailedEvent) {
        return applicationFailedEvent.getApplicationContext() != null ? applicationFailedEvent.getApplicationContext().getEnvironment() : null;
    }
}
