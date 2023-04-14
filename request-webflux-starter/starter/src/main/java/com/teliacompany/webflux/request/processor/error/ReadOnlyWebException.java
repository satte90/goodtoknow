package com.teliacompany.webflux.request.processor.error;

import com.teliacompany.webflux.error.exception.WebException;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public final class ReadOnlyWebException {
    private final WebException webException;

    public ReadOnlyWebException(WebException webException) {
        this.webException = webException;
    }

    public HttpStatus getStatus() {
        return webException.getStatus();
    }

    public Optional<String> getCauseMessage() {
        return webException.getCauseMessage();
    }

    public Optional<String> getCauseSystem() {
        return webException.getCauseSystem();
    }

    public Optional<String> getCauseTransactionId() {
        return webException.getCauseTransactionId();
    }

    public Optional<Integer> getCauseHttpStatus() {
        return webException.getCauseHttpStatus();
    }

    public String getMessage() {
        return webException.getMessage();
    }

    public Map<String, Object> getErrorAttributes() {
        return Collections.unmodifiableMap(webException.getExtraAttributes());
    }

    /**
     * If you need to check if an attribute exist already, use this rather than getErrorAttributes().contains as this will create a new unmodifiable map
     */
    public boolean hasAttribute(String attributeKey) {
        return webException.getExtraAttributes().containsKey(attributeKey);
    }


}
