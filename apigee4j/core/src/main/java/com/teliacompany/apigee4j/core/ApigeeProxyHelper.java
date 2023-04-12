package com.teliacompany.apigee4j.core;

import com.teliacompany.apigee4j.core.config.ApigeeConnectionConfig;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.SslProvider;
import reactor.netty.transport.ProxyProvider.Proxy;

@Component
public class ApigeeProxyHelper {

    private final ApigeeConnectionConfig apigeeConnectionConfig;
    private final Environment environment;

    public ApigeeProxyHelper(ApigeeConnectionConfig apigeeConnectionConfig, Environment environment) {
        this.apigeeConnectionConfig = apigeeConnectionConfig;
        this.environment = environment;
    }

    public ReactorClientHttpConnector getReactorClientHttpConnector() {
        return new ReactorClientHttpConnector(getHttpClient());
    }

    private HttpClient getHttpClient() {
        return HttpClient.create().proxy(
                proxy -> proxy.type(Proxy.HTTP)
                        .host(apigeeConnectionConfig.proxyHost)
                        .port(apigeeConnectionConfig.proxyPort)
        );
    }

    public boolean isProxyActive() {
        return apigeeConnectionConfig.proxyEnabled;
    }
}
