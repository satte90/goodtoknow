package com.teliacompany.webflux.request.client;

import org.springframework.context.annotation.DependsOn;

/**
 * Implement this in your client config where you know the base URL and client service name.
 */
@DependsOn({"AugustusAppConfig"})
public interface WebClientEndpoint {
    String getServiceName();

    String getEndpoint();
}
