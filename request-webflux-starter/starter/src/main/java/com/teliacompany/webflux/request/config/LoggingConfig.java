package com.teliacompany.webflux.request.config;

import com.teliacompany.webflux.request.filter.JsonPayloadLoggingFilter;
import com.teliacompany.webflux.request.filter.JsonPayloadLoggingFilter.FilterFunction;
import com.teliacompany.webflux.request.filter.HeadersLoggingFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties("logging")
public class LoggingConfig {
    private static final Logger LOG = LoggerFactory.getLogger(LoggingConfig.class);

    private boolean requestPayloadLoggingEnabled = true;
    private boolean responsePayloadLoggingEnabled = true;
    private HeadersLoggingFilter headersFilter = HeadersLoggingFilter.defaultFilter();

    private List<String> includePaths = new ArrayList<>();
    private List<String> ignorePaths = new ArrayList<>();
    private List<Integer> ignoreResponseCodes = new ArrayList<>();
    private List<String> filterHeaders = null;
    private boolean logAsObjectMessage = true;
    private boolean encodePayload = true;
    private boolean encodeHeaders = true;

    private int maxPayloadLoggingLength = 10000;
    private int maxEncodedPayloadLoggingLength = maxPayloadLoggingLength * 10;

    @PostConstruct
    public void init() {
        if(filterHeaders != null) {
            headersFilter = new HeadersLoggingFilter();
            filterHeaders.forEach(h -> headersFilter.add(h));
        }

        if(maxEncodedPayloadLoggingLength <= maxPayloadLoggingLength) {
            maxEncodedPayloadLoggingLength = maxPayloadLoggingLength * 10;
        }

        LOG.info("*************************");
        LOG.info("Logging config loaded:");
        LOG.info("requestPayloadLoggingEnabled: {}", requestPayloadLoggingEnabled);
        LOG.info("responsePayloadLoggingEnabled: {}", responsePayloadLoggingEnabled);
        LOG.info("headersFilter: {}", String.join(", ", headersFilter.getHeaderNameFilters()));
        LOG.info("includePaths: {}", includePaths);
        LOG.info("ignorePaths: {}", ignorePaths);
        LOG.info("ignoreResponseCodes: {}", ignoreResponseCodes);
        LOG.info("encodeHeaders: {}", encodeHeaders);
        LOG.info("encodePayload: {}", encodePayload);
        LOG.info("maxPayloadLoggingLength: {}", maxPayloadLoggingLength);
        LOG.info("maxEncodedPayloadLoggingLength: {}", maxEncodedPayloadLoggingLength);
        LOG.info("logAsObjectMessage: {}", logAsObjectMessage);
        LOG.info("*************************");
    }

    public void setRequestPayloadLoggingEnabled(boolean requestPayloadLoggingEnabled) {
        this.requestPayloadLoggingEnabled = requestPayloadLoggingEnabled;
    }

    public void setResponsePayloadLoggingEnabled(boolean responsePayloadLoggingEnabled) {
        this.responsePayloadLoggingEnabled = responsePayloadLoggingEnabled;
    }

    public void setHeadersFilter(HeadersLoggingFilter headersFilter) {
        this.headersFilter = headersFilter;
    }

    public void setIncludePaths(List<String> includePaths) {
        this.includePaths = includePaths;
    }

    public void setIgnorePaths(List<String> ignorePaths) {
        this.ignorePaths = ignorePaths;
    }

    public void setIgnoreResponseCodes(List<Integer> ignoreResponseCodes) {
        this.ignoreResponseCodes = ignoreResponseCodes;
    }

    public void setFilterHeaders(List<String> filterHeaders) {
        this.filterHeaders = filterHeaders;
    }

    public void setLogAsObjectMessage(boolean logAsObjectMessage) {
        this.logAsObjectMessage = logAsObjectMessage;
    }

    public boolean isRequestPayloadLoggingEnabled() {
        return requestPayloadLoggingEnabled;
    }

    public boolean isResponsePayloadLoggingEnabled() {
        return responsePayloadLoggingEnabled;
    }

    public HeadersLoggingFilter getHeadersFilter() {
        return headersFilter;
    }

    public List<String> getIncludePaths() {
        return includePaths;
    }

    public List<String> getIgnorePaths() {
        return ignorePaths;
    }

    public List<Integer> getIgnoreResponseCodes() {
        return ignoreResponseCodes;
    }

    public List<String> getFilterHeaders() {
        return filterHeaders;
    }

    public boolean isLogAsObjectMessage() {
        return logAsObjectMessage;
    }

    public LoggingConfigBuilder mutate() {
        return new LoggingConfigBuilder(this);
    }

    public boolean isEncodePayload() {
        return encodePayload;
    }

    public void setEncodePayload(boolean encodePayload) {
        this.encodePayload = encodePayload;
    }

    public boolean isEncodeHeaders() {
        return encodeHeaders;
    }

    public void setEncodeHeaders(boolean encodeHeaders) {
        this.encodeHeaders = encodeHeaders;
    }

    public int getMaxPayloadLoggingLength() {
        return maxPayloadLoggingLength;
    }

    public void setMaxPayloadLoggingLength(int maxPayloadLoggingLength) {
        this.maxPayloadLoggingLength = maxPayloadLoggingLength;
    }

    public int getMaxEncodedPayloadLoggingLength() {
        return maxEncodedPayloadLoggingLength;
    }

    public void setMaxEncodedPayloadLoggingLength(int maxEncodedPayloadLoggingLength) {
        this.maxEncodedPayloadLoggingLength = maxEncodedPayloadLoggingLength;
    }

    public static class LoggingConfigBuilder {
        private final boolean responsePayloadLoggingEnabled;
        private boolean requestPayloadLoggingEnabled;
        private HeadersLoggingFilter headersFilter;
        private JsonPayloadLoggingFilter payloadLoggingFilter;
        private List<String> includePaths;
        private List<String> ignorePaths;
        private List<Integer> ignoreResponseCodes;
        private boolean logAsObjectMessage;

        private int maxPayloadLoggingLength;

        public LoggingConfigBuilder(LoggingConfig loggingConfig) {
            this.requestPayloadLoggingEnabled = loggingConfig.requestPayloadLoggingEnabled;
            this.responsePayloadLoggingEnabled = loggingConfig.responsePayloadLoggingEnabled;
            this.headersFilter = new HeadersLoggingFilter();
            loggingConfig.headersFilter.getHeaderNameFilters().forEach(h -> this.headersFilter.add(h));
            this.payloadLoggingFilter = new JsonPayloadLoggingFilter();
            this.includePaths = new ArrayList<>(loggingConfig.includePaths);
            this.ignorePaths = new ArrayList<>(loggingConfig.ignorePaths);
            this.ignoreResponseCodes = new ArrayList<>(loggingConfig.ignoreResponseCodes);
            this.logAsObjectMessage = loggingConfig.logAsObjectMessage;
            this.maxPayloadLoggingLength = loggingConfig.maxPayloadLoggingLength;
        }

        public LoggingConfigBuilder setRequestPayloadLoggingEnabled(boolean requestPayloadLoggingEnabled) {
            this.requestPayloadLoggingEnabled = requestPayloadLoggingEnabled;
            return this;
        }

        public LoggingConfigBuilder setResponsePayloadLoggingEnabled(boolean responsePayloadLoggingEnabled) {
            this.requestPayloadLoggingEnabled = responsePayloadLoggingEnabled;
            return this;
        }

        public LoggingConfigBuilder setHeadersFilter(HeadersLoggingFilter headersFilter) {
            this.headersFilter = headersFilter;
            return this;
        }

        public LoggingConfigBuilder filterHeader(String header) {
            this.headersFilter.add(header);
            return this;
        }

        public LoggingConfigBuilder setPayloadLoggingFilter(JsonPayloadLoggingFilter payloadLoggingFilter) {
            this.payloadLoggingFilter = payloadLoggingFilter;
            return this;
        }

        public LoggingConfigBuilder filterJsonAttribute(FilterFunction function, String regex) {
            this.payloadLoggingFilter.add(function, regex);
            return this;
        }

        public LoggingConfigBuilder setIncludePaths(List<String> includePaths) {
            this.includePaths = includePaths;
            return this;
        }

        public LoggingConfigBuilder includePath(String path) {
            if(this.includePaths == null) {
                this.includePaths = new ArrayList<>();
            }
            this.includePaths.add(path);
            return this;
        }

        public LoggingConfigBuilder setIgnorePaths(List<String> ignorePaths) {
            this.ignorePaths = ignorePaths;
            return this;
        }

        public LoggingConfigBuilder ignorePath(String path) {
            if(this.ignorePaths == null) {
                this.ignorePaths = new ArrayList<>();
            }
            this.ignorePaths.add(path);
            return this;
        }

        public LoggingConfigBuilder setIgnoreResponseCodes(List<Integer> ignoreResponseCodes) {
            this.ignoreResponseCodes = ignoreResponseCodes;
            return this;
        }

        public LoggingConfigBuilder ignoreResponseCode(HttpStatus httpStatusCode) {
            if(this.ignoreResponseCodes == null) {
                this.ignoreResponseCodes = new ArrayList<>();
            }
            this.ignoreResponseCodes.add(httpStatusCode.value());
            return this;
        }

        public LoggingConfigBuilder setLogAsObjectMessage(boolean logAsObjectMessage) {
            this.logAsObjectMessage = logAsObjectMessage;
            return this;
        }

        public LoggingConfigBuilder setMaxPayloadLoggingLength(int maxPayloadLoggingLength) {
            this.maxPayloadLoggingLength = maxPayloadLoggingLength;
            return this;
        }

        public LoggingConfig build() {
            LoggingConfig config = new LoggingConfig();
            config.requestPayloadLoggingEnabled = requestPayloadLoggingEnabled;
            config.responsePayloadLoggingEnabled = responsePayloadLoggingEnabled;
            config.headersFilter = headersFilter;
            config.includePaths = includePaths;
            config.ignorePaths = ignorePaths;
            config.ignoreResponseCodes = ignoreResponseCodes;
            config.logAsObjectMessage = logAsObjectMessage;
            config.maxPayloadLoggingLength = maxPayloadLoggingLength;
            return config;
        }
    }


}
