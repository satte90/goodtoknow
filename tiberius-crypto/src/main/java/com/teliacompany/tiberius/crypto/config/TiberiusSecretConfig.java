package com.teliacompany.tiberius.crypto.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.Optional;

@Configuration
public class TiberiusSecretConfig {

    private static final String PROPERTIES_SECRET_KEY = "tiberius.properties.secret";
    private static final String ENV_VARIABLE_SECRET_KEY = "TIBERIUS_PROPERTIES_SECRET";

    @Value("${" + PROPERTIES_SECRET_KEY + ":#{null}}")
    private String propertiesSecretKey;

    @PostConstruct
    public void init() {
        //The secret is not set in property files for other environments than dev/compinenttest/minikube. Try to read it instead from system properties and env properties.
        //Prio: envVar, sysProp, appProp
        final String envSecret = System.getenv(ENV_VARIABLE_SECRET_KEY);
        this.propertiesSecretKey = Optional.ofNullable(envSecret).orElse(System.getProperty(PROPERTIES_SECRET_KEY, propertiesSecretKey));
    }

    public String getPropertiesSecretKey() {
        return propertiesSecretKey;
    }
}
