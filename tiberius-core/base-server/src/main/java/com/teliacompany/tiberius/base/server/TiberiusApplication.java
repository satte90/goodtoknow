package com.teliacompany.tiberius.base.server;

import com.teliacompany.tiberius.base.server.config.ConfigReactor;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.reactive.config.EnableWebFlux;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootApplication
@EnableScheduling
@EnableAsync
@EnableWebFlux
@Import({TiberiusScriba.class, ConfigReactor.class})
@ComponentScan(basePackages = {"com.teliacompany.tiberius"})
public @interface TiberiusApplication {

    /**
     * Determine application name based on class file name.
     * Turn TiberiusApplicationName into tiberius-application-name (TiberiusSubscription -> tiberius-subscription)
     */
    String AUTOMATIC_APPLICATION_NAME = "auto";

    /**
     * Set the application name for the service, by default the application name will be determined based on the class name. This can be overridden by
     * the property spring.application.name
     */
    String applicationName() default AUTOMATIC_APPLICATION_NAME;

}
