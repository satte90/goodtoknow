package com.teliacompany.tiberius.base.server.event;

import com.teliacompany.tiberius.base.utils.TiberiusEventOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.PropertySource;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Event running last of all tiberius events for that type, in this case ApplicationEnvironmentPreparedEvent.
 *
 * Event simply logs the property sources in prio order
 */
@SuppressWarnings("DefaultAnnotationParam")
@Order(TiberiusEventOrder.ENVIRONMENT_PREPARED_SUMMARY)
public class TiberiusEnvPreparedSummaryEvent implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(TiberiusEnvPreparedSummaryEvent.class);

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        String sources = event.getEnvironment().getPropertySources().stream()
                .map(this::getPrettyName)
                .collect(Collectors.joining(", "));
        LOG.info("PropertySources in priority: {}", sources);
    }

    private String getPrettyName(PropertySource<?> propertySource) {
        final String name = propertySource.getName();
        // ApplicationProperties get pretty ugly name like this: // Config resource 'class path resource [application-dev.properties]' via location 'optional:classpath:/'
        // Just extract the part inside square brackets []
        if(name.contains("[application")) {
            Pattern uglyNamePattern = Pattern.compile(".*\\[(application.*)\\.properties].*");
            Matcher m = uglyNamePattern.matcher(name);
            if(m.find() && m.groupCount() > 0) {
                return m.group(1);
            }
        }
        return name;
    }



}

