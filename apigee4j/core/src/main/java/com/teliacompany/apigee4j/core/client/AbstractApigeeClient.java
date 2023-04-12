package com.teliacompany.apigee4j.core.client;

import com.teliacompany.apigee4j.core.ApigeeOAuth2ExchangeFilter;
import com.teliacompany.apigee4j.core.ApigeeProxyHelper;
import com.teliacompany.apigee4j.core.config.ApigeeConnectionConfig;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import javax.annotation.PostConstruct;

/**
 * Abstract WebClient for Apigee integrations. Automatically adds oauth filter and client config baseUrl.
 * To be extended by clients in Apigee integration modules.
 */
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
@Component
public abstract class AbstractApigeeClient<C extends ApigeeClientConfig> implements WebClient {
    private WebClient webClient;
    private final C clientConfig;

    @Autowired
    private ApigeeOAuth2ExchangeFilter apigeeOAuth2ExchangeFilter;

    @Autowired
    protected ApigeeConnectionConfig apigeeConnectionConfig;

    @Autowired
    private ApigeeProxyHelper proxyHelper;

    protected AbstractApigeeClient(C config) {
        this.clientConfig = config;
    }

    @PostConstruct
    public void init() {
        final String url = apigeeConnectionConfig.apigeeHost + clientConfig.getEndpoint();
        logInit(url);
        Builder builder = WebClient
            .builder()
            .baseUrl(url)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .filter(apigeeOAuth2ExchangeFilter);

        if (proxyHelper.isProxyActive()) {
            builder.clientConnector(proxyHelper.getReactorClientHttpConnector());
        }

        webClient = builder.build();
    }

    void logInit(String url) {
        final Class<?> superClazz = this.getClass().getSuperclass();
        if (superClazz != null) {
            LoggerFactory.getLogger(superClazz).info("apigee4j client for \"{}\" loaded with endpoint: \"{}\"", clientConfig.getServiceName(), url);
        }
    }

    @Override
    public RequestHeadersUriSpec<?> get() {
        return webClient.get();
    }

    @Override
    public RequestHeadersUriSpec<?> head() {
        return webClient.head();
    }

    @Override
    public RequestBodyUriSpec post() {
        return webClient.post();
    }

    @Override
    public RequestBodyUriSpec put() {
        return webClient.put();
    }

    @Override
    public RequestBodyUriSpec patch() {
        return webClient.patch();
    }

    @Override
    public RequestHeadersUriSpec<?> delete() {
        return webClient.delete();
    }

    @Override
    public RequestHeadersUriSpec<?> options() {
        return webClient.options();
    }

    @Override
    public RequestBodyUriSpec method(HttpMethod method) {
        return webClient.method(method);
    }

    @Override
    public Builder mutate() {
        return webClient.mutate();
    }

    public C getClientConfig() {
        return clientConfig;
    }

    /**
     * Shortcut for getClientConfig().getServiceName()
     * @return
     */
    public String getServiceName() {
        return clientConfig.getServiceName();
    }
}
