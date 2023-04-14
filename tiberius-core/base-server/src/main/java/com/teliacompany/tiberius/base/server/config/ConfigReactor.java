package com.teliacompany.tiberius.base.server.config;

import com.teliacompany.webflux.jackson.JacksonAutoConfiguration;
import com.teliacompany.tiberius.base.server.TiberiusApplication;
import com.teliacompany.tiberius.base.server.config.security.TiberiusSecurityConfig;
import com.teliacompany.tiberius.base.server.event.TiberiusClosedEvent;
import com.teliacompany.tiberius.base.server.event.TiberiusShutdownEvent;
import com.teliacompany.tiberius.base.server.event.TiberiusStartupEvent;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Configuration Reactor file. Automatically loaded from spring.factories file
 * ComponentScan - In base.server.** package, loads devops service and testsupport
 * Import - other needed config files
 * PropertySource - Set property files to read
 */
@Configuration(value = "TiberiusConfiguration")
@ComponentScan(basePackageClasses = TiberiusApplication.class)
@Import({
        BasePathConfiguration.class,
        VersionProperties.class,
        JacksonAutoConfiguration.class,
        RegistrationProperties.class,
        ApplicationProperties.class,
        TiberiusSecurityConfig.class,
        SecretConfig.class,
        SwaggerConfig.class,
        SpringDocConfig.class,
        TiberiusStartupEvent.class,
        TiberiusShutdownEvent.class,
        TiberiusClosedEvent.class
})
public class ConfigReactor {

}
