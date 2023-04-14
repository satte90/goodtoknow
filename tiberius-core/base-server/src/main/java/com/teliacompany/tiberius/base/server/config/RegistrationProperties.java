package com.teliacompany.tiberius.base.server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RegistrationProperties {
    private final boolean serviceRegistrationEnabled;
    private final String serviceRegistrationName;
    private final String serviceRegistrationHost;
    private final String serviceRegistrationPath;
    private final long serviceRegistrationInterval;

    public RegistrationProperties(@Value("${tiberius.praefectus.register}") boolean serviceRegistrationEnabled,
                                  @Value("${tiberius.praefectus.register.name:Praefectus}") String serviceRegistrationName,
                                  @Value("${tiberius.praefectus.register.host}") String serviceRegistrationHost,
                                  @Value("${tiberius.praefectus.register.path}") String serviceRegistrationPath,
                                  @Value("${tiberius.praefectus.register.interval:600000}") long serviceRegistrationInterval) {
        this.serviceRegistrationEnabled = serviceRegistrationEnabled;
        this.serviceRegistrationName = serviceRegistrationName;
        this.serviceRegistrationHost = serviceRegistrationHost;
        this.serviceRegistrationPath = serviceRegistrationPath;
        this.serviceRegistrationInterval = serviceRegistrationInterval;
    }

    public boolean isServiceRegistrationEnabled() {
        return serviceRegistrationEnabled;
    }

    public String getServiceRegistrationPath() {
        return serviceRegistrationPath;
    }

    public long getServiceRegistrationInterval() {
        return serviceRegistrationInterval;
    }

    public String getServiceRegistrationHost() {
        return serviceRegistrationHost;
    }

    public String getServiceRegistrationName() {
        return serviceRegistrationName;
    }
}
