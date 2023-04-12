package com.teliacompany.springfield.managetelephone.facade.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = {
        "classpath:/manage-telephone-number-facade-client.properties",
        "classpath:/manage-telephone-number-facade-client-${spring.profiles.active}.properties",
        "classpath:/manage-telephone-number-facade-client-${spring.profiles.main}.properties"
}, ignoreResourceNotFound = true)
public class ManageTelephoneNumberFacadeClientConfiguration {
    public static final String SERVICE_NAME = "ManageTelephoneNumber";
    @Value("${manage.telephone.number.host}")
    private String host;

    @Value("${manage.telephone.number.endpoint}")
    private String endpoint;

    public String getHost() {
        return host;
    }

    public String getEndpoint() {
        return endpoint;
    }


}
