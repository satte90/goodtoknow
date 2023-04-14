package com.teliacompany.webflux.request.client;

import com.teliacompany.webflux.request.client.WebClientConfig.Builder.WebClientConfigBuilderStep1;
import com.teliacompany.webflux.request.log.RequestLoggingOptions;
import org.apache.commons.lang3.StringUtils;

public class WebClientConfig {
    private final String host;
    private final String basePath;
    private final String serviceName;
    private final RequestLoggingOptions requestLoggingOptions;

    private final boolean allowUnsecureSslConnection;
    private final boolean proxyEnabled;
    private final String proxyHost;
    private final Integer proxyPort;

    private WebClientConfig(String serviceName,
                            String host,
                            String basePath,
                            RequestLoggingOptions requestLoggingOptions,
                            boolean proxyEnabled,
                            String proxyHost,
                            Integer proxyPort,
                            boolean allowUnsecureSslConnection) {
        // Never have a trailing slash on host
        this.host = StringUtils.stripEnd(host, "/");
        this.requestLoggingOptions = requestLoggingOptions;
        this.proxyEnabled = proxyEnabled;
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        // Only have one leading slash on basePath, no trailing slash
        if(basePath == null) {
            this.basePath = "";
        } else {
            this.basePath = "/" + StringUtils.strip(basePath, "/");
        }
        this.serviceName = serviceName;

        this.allowUnsecureSslConnection = allowUnsecureSslConnection;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static WebClientConfigBuilderStep1 withServiceName(String serviceName) {
        return new Builder().withServiceName(serviceName);
    }

    public String getBasePath() {
        return basePath;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getHost() {
        return host;
    }

    public String getUrl() {
        return host + basePath;
    }

    public RequestLoggingOptions getRequestLoggingOptions() {
        return requestLoggingOptions;
    }

    public boolean isProxyEnabled() {
        return proxyEnabled;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public Integer getProxyPort() {
        return proxyPort;
    }

    public boolean allowUnsecureSslConnection() {
        return allowUnsecureSslConnection;
    }

    public static class Builder {
        private String host;
        private String basePath;
        private String serviceName;
        private RequestLoggingOptions requestLoggingOptions = RequestLoggingOptions.defaults();

        private boolean allowUnsecureSslConnection = false;
        private boolean proxyEnabled = false;
        private String proxyHost = null;
        private Integer proxyPort = null;

        public WebClientConfigBuilderStep1 withServiceName(String serviceName) {
            this.serviceName = serviceName;
            return new WebClientConfigBuilderStep1();
        }

        public final class WebClientConfigBuilderStep1 {
            private WebClientConfigBuilderStep1() {
            }

            public WebClientConfigBuilderFinalStep withHost(String value) {
                host = value;
                return new WebClientConfigBuilderFinalStep();
            }
        }

        public final class WebClientConfigBuilderFinalStep {
            private WebClientConfigBuilderFinalStep() {
            }

            public WebClientConfigBuilderFinalStep withBasePath(String value) {
                basePath = StringUtils.removeEnd(value, "/");
                return this;
            }

            public WebClientConfigBuilderFinalStep withLoggingOptions(RequestLoggingOptions value) {
                requestLoggingOptions = value;
                return this;
            }

            public WebClientConfigBuilderFinalStep allowUnsecureSslConnection(boolean value) {
                allowUnsecureSslConnection = value;
                return this;
            }

            public WebClientConfigBuilderFinalStep withProxyEnabled(boolean enabled, String proxy, int port) {
                if(enabled) {
                    proxyEnabled = true;
                    proxyHost = proxy;
                    proxyPort = port;
                }
                return this;
            }

            public WebClientConfig build() {
                return new WebClientConfig(serviceName, host, basePath, requestLoggingOptions, proxyEnabled, proxyHost, proxyPort, allowUnsecureSslConnection);
            }
        }
    }
}
