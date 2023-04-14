package com.teliacompany.webflux.request.processor.error;

import com.teliacompany.webflux.request.context.ContextWrapper;

import java.util.Map;

public interface ErrorAttributesProvider {
    Map<String, Object> getErrorAttributes(ReadOnlyWebException webException, ContextWrapper contextData);
}
