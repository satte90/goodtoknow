package com.teliacompany.springfield.addressmaster.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = {
        "classpath:/addressmaster-client.properties",
        "classpath:/addressmaster-client-${spring.profiles.active}.properties",
        "classpath:/addressmaster-client-${spring.profiles.main}.properties"
}, ignoreResourceNotFound = true)
public class AddressMasterClientConfiguration {
    public static final String SERVICE_NAME = "AddressMaster";

    private final String host;
    private final String endpoint;
    private final String username;
    private final String password;

    public AddressMasterClientConfiguration(
            @Value("${addressmaster.host}") String host,
            @Value("${addressmaster.endpoint}") String endpoint,
            @Value("${addressmaster.username}") String username,
            @Value("${addressmaster.password}") String password) {
        this.host = host;
        this.endpoint = endpoint;
        this.username = username;
        this.password = password;
    }

    public String getHost() {
        return host;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
