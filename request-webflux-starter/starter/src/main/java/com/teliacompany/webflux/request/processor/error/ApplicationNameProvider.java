package com.teliacompany.webflux.request.processor.error;

import com.teliacompany.webflux.error.api.ErrorAttribute;
import com.teliacompany.webflux.request.context.ContextWrapper;

import java.util.Map;

/**
 * Add applicationName property if it does not already exist on webException
 */
public class ApplicationNameProvider implements ErrorAttributesProvider {
    private final String applicationName;

    public ApplicationNameProvider(String applicationName) {
        this.applicationName = applicationName;
    }

    @Override
    public Map<String, Object> getErrorAttributes(ReadOnlyWebException webException, ContextWrapper contextData) {
        if(!webException.hasAttribute(ErrorAttribute.APPLICATION_NAME)) {
            return Map.of(ErrorAttribute.APPLICATION_NAME, applicationName);
        }
        return Map.of();
    }

}
