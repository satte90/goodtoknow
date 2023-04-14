package com.teliacompany.webflux.request.log;

import com.teliacompany.webflux.request.filter.JsonPayloadLoggingFilter;
import org.apache.logging.log4j.Level;

public class RequestLoggingOptions {
    public enum PayloadLoggingOption {
        TRUE, FALSE, DEFAULT
    }

    private PayloadLoggingOption requestPayloadLoggingOption;
    private PayloadLoggingOption responsePayloadLoggingOption;
    private Level logLevel;

    private JsonPayloadLoggingFilter payloadLoggingFilter;

    private boolean logging = true;

    public RequestLoggingOptions(PayloadLoggingOption payloadLoggingOption, Level logLevel) {
        this.requestPayloadLoggingOption = payloadLoggingOption;
        this.responsePayloadLoggingOption = payloadLoggingOption;
        this.logLevel = logLevel;

        this.payloadLoggingFilter = JsonPayloadLoggingFilter.empty();
    }

    public RequestLoggingOptions(PayloadLoggingOption payloadLoggingOption, Level logLevel, JsonPayloadLoggingFilter payloadLoggingFilter) {
        this.requestPayloadLoggingOption = payloadLoggingOption;
        this.responsePayloadLoggingOption = payloadLoggingOption;
        this.logLevel = logLevel;
        this.payloadLoggingFilter = payloadLoggingFilter;
    }

    public static RequestLoggingOptions defaults() {
        return new RequestLoggingOptions(PayloadLoggingOption.DEFAULT, Level.INFO, JsonPayloadLoggingFilter.empty());
    }

    public PayloadLoggingOption getRequestPayloadLoggingOption() {
        return requestPayloadLoggingOption;
    }

    public RequestLoggingOptions setRequestPayloadLoggingOption(PayloadLoggingOption requestPayloadLoggingOption) {
        this.requestPayloadLoggingOption = requestPayloadLoggingOption;
        return this;
    }

    public PayloadLoggingOption getResponsePayloadLoggingOption() {
        return responsePayloadLoggingOption;
    }

    public RequestLoggingOptions setResponsePayloadLoggingOption(PayloadLoggingOption responsePayloadLoggingOption) {
        this.responsePayloadLoggingOption = responsePayloadLoggingOption;
        return this;
    }

    public Level getLogLevel() {
        return logLevel;
    }

    public RequestLoggingOptions setLogLevel(Level logLevel) {
        this.logLevel = logLevel;
        return this;
    }

    public JsonPayloadLoggingFilter getPayloadLoggingFilter() {
        return payloadLoggingFilter;
    }

    public void setPayloadLoggingFilter(JsonPayloadLoggingFilter payloadLoggingFilter) {
        this.payloadLoggingFilter = payloadLoggingFilter;
    }

    public RequestLoggingOptions disableLogging() {
        this.logging = false;
        return this;
    }

    public boolean isLoggingEnabled() {
        return logging;
    }

}
