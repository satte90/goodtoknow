package com.teliacompany.webflux.request.client;

import com.teliacompany.apigee4j.core.client.AbstractApigeeClient;
import com.teliacompany.apigee4j.core.client.ApigeeClientConfig;
import com.teliacompany.apimarket4j.core.client.AbstractApiMarket4jClient;
import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient.Builder;
import org.springframework.web.util.UriBuilderFactory;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;
import reactor.netty.transport.ProxyProvider.Proxy;

import javax.net.ssl.SSLException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

public final class WebClientBuilder {
    private final Builder realWebClientBuilder;
    private final WebClientConfig clientConfig;
    private final HttpHeaders defaultHeaders = new HttpHeaders();

    private Duration timeoutDuration = Duration.ofSeconds(30);
    private boolean circuitBreakerEnabled = true;
    private boolean forwardAuthorization = false;
    private ErrorStatusHandler.ClassToErrorMapper<?, ? extends Throwable> errorConverter;

    /**
     * Create a new WebClient from an ApiMarket4jClient.
     */
    public static WebClientBuilder withApiMarketClient(AbstractApiMarket4jClient<?> client) {
        Builder builder = client.mutate();
        WebClientConfig clientConfig = WebClientConfig
                .withServiceName(client.getServiceName())
                .withHost(client.getClientConfig().getHost())
                .withBasePath(client.getClientConfig().getEndpoint())
                .build();
        return new WebClientBuilder(builder, clientConfig);
    }

    /**
     * Create a new WebClient from an Apigee4jClient.
     */
    public static WebClientBuilder withApigeeClient(AbstractApigeeClient<?> client) {
        Builder builder = client.mutate();
        final ApigeeClientConfig apigeeClientConfig = client.getClientConfig();
        WebClientConfig clientConfig = WebClientConfig
                .withServiceName(client.getServiceName())
                .withHost(apigeeClientConfig.getHost())
                .withBasePath(apigeeClientConfig.getEndpoint())
                .withProxyEnabled(apigeeClientConfig.isProxyEnabled(), apigeeClientConfig.getProxyHost(), apigeeClientConfig.getProxyPort())
                .build();
        return new WebClientBuilder(builder, clientConfig);
    }

    public static WebClientBuilder withConfig(WebClientConfig clientConfig) {
        final org.springframework.web.reactive.function.client.WebClient webClient = org.springframework.web.reactive.function.client.WebClient.builder()
                .baseUrl(clientConfig.getUrl())
                .defaultHeader(CONTENT_TYPE, "application/json;charset=UTF-8")
                .build();

        Builder builder = webClient.mutate();
        return new WebClientBuilder(builder, clientConfig);
    }

    private WebClientBuilder(Builder realWebClientBuilder, WebClientConfig clientConfig) {
        this.realWebClientBuilder = realWebClientBuilder;
        this.clientConfig = clientConfig;
    }

    public WebClientBuilder defaultUriVariables(Map<String, ?> defaultUriVariables) {
        realWebClientBuilder.defaultUriVariables(defaultUriVariables);
        return this;
    }

    public WebClientBuilder uriBuilderFactory(UriBuilderFactory uriBuilderFactory) {
        realWebClientBuilder.uriBuilderFactory(uriBuilderFactory);
        return this;
    }

    public WebClientBuilder defaultHeader(String headerName, String... headerValues) {
        realWebClientBuilder.defaultHeader(headerName, headerValues);
        defaultHeaders.addAll(headerName, Arrays.asList(headerValues));
        return this;
    }

    public WebClientBuilder defaultCookie(String cookieName, String... cookieValues) {
        realWebClientBuilder.defaultCookie(cookieName, cookieValues);
        return this;
    }

    public WebClientBuilder clientConnector(ClientHttpConnector connector) {
        realWebClientBuilder.clientConnector(connector);
        return this;
    }

    public WebClientBuilder filter(ExchangeFilterFunction filter) {
        realWebClientBuilder.filter(filter);
        return this;
    }

    public WebClientBuilder exchangeFunction(ExchangeFunction exchangeFunction) {
        realWebClientBuilder.exchangeFunction(exchangeFunction);
        return this;
    }

    public WebClientBuilder exchangeStrategies(ExchangeStrategies strategies) {
        realWebClientBuilder.exchangeStrategies(strategies);
        return this;
    }

    public WebClientBuilder apply(Consumer<Builder> builderConsumer) {
        realWebClientBuilder.apply(builderConsumer);
        return this;
    }

    public WebClientBuilder timeout(Duration duration) {
        this.timeoutDuration = duration;
        return this;
    }

    public WebClientBuilder disableCurcuitBreaker() {
        this.circuitBreakerEnabled = false;
        return this;
    }

    public WebClientBuilder forwardAuthorization() {
        this.forwardAuthorization = true;
        return this;
    }

    /**
     * Overrides the default error handler for the web client. This can also be set per request.
     * Convert error class to error string message.
     * Exception is automatically created based on http status code.
     *
     * @param clazz     - class to map root cause error to
     * @param converter - mapping function to map the error -> string
     */
    public <T, X extends Throwable> WebClientBuilder errorConverter(Class<T> clazz, Function<WebClientResponse<T>, X> converter) {
        this.errorConverter = new ErrorStatusHandler.ClassToErrorMapper<>(clazz, converter);
        return this;
    }

    public WebClient build() {
        int timeoutExtra = circuitBreakerEnabled ? 1111 : 0; // If circuit breaker is enabled, we rely on the timeout in it instead of this. so we add ~1 second to be sure.
        final int timeoutMs = (int) (timeoutDuration.toMillis() + timeoutExtra);
        TcpClient tcpClient = TcpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeoutMs) // Connection Timeout
                .doOnConnected(connection ->
                        connection.addHandlerLast(new ReadTimeoutHandler(timeoutMs, TimeUnit.MILLISECONDS)) // Read Timeout
                                .addHandlerLast(new WriteTimeoutHandler(timeoutMs, TimeUnit.MILLISECONDS))); // Write Timeout

        HttpClient httpClient = HttpClient.from(tcpClient);

        if(clientConfig.isProxyEnabled()) {
            httpClient = httpClient.proxy(typeSpec -> typeSpec.type(Proxy.HTTP)
                    .host(clientConfig.getProxyHost())
                    .port(clientConfig.getProxyPort())
            );
        }

        if(clientConfig.allowUnsecureSslConnection()) {
            SslContext sslContext = getInsecureSslContext();
            if(sslContext != null) {
                httpClient = httpClient.secure(t -> t.sslContext(sslContext));
            }
        }

        ClientHttpConnector connector = new ReactorClientHttpConnector(httpClient);

        final ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(5242880)) //5 mb
                .build();

        WebClientConstructionPlan constructionPlan = new WebClientConstructionPlan(realWebClientBuilder, exchangeStrategies, connector);
        WebClient webClient = new WebClient(constructionPlan,
                defaultHeaders,
                clientConfig,
                errorConverter,
                timeoutDuration,
                circuitBreakerEnabled,
                forwardAuthorization
        );
        WebClientRegistry.register(webClient);
        WebClientInitializer.initializeWebClient(webClient);
        return webClient;
    }

    private SslContext getInsecureSslContext() {
        try {
           return SslContextBuilder
                    .forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .build();
        } catch(SSLException e) {
            return null;
        }
    }

}
