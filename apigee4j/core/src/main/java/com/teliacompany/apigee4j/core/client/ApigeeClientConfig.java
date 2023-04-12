package com.teliacompany.apigee4j.core.client;

import com.teliacompany.apigee4j.core.config.ApigeeConnectionConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
@Component
public abstract class ApigeeClientConfig {

    public abstract String getServiceName();

    public abstract String getEndpoint();

    @Autowired
    protected ApigeeConnectionConfig apigeeConnectionConfig;

    public String getUrl() {
        return apigeeConnectionConfig.apigeeHost + getEndpoint();
    }

    public String getHost() {
        return apigeeConnectionConfig.apigeeHost;
    }


}
