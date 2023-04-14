package com.teliacompany.webflux.request.client;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teliacompany.webflux.error.exception.WebException;
import com.teliacompany.webflux.error.exception.server.BadGatewayException;
import com.teliacompany.webflux.error.exception.server.GatewayTimeoutException;
import com.teliacompany.webflux.error.exception.server.InternalServerErrorException;
import com.teliacompany.webflux.error.exception.server.ServiceUnavailableException;
import com.teliacompany.webflux.jackson.TeliaObjectMapper;
import com.teliacompany.webflux.request.RequestProcessor;
import com.teliacompany.webflux.request.client.ErrorStatusHandler.ClassToErrorMapper;
import com.teliacompany.webflux.request.context.RequestContext;
import com.teliacompany.webflux.request.context.RequestContextBuilder;
import com.teliacompany.webflux.request.filter.JsonPayloadLoggingFilter;
import com.teliacompany.webflux.request.log.DisabledRequestLogger;
import com.teliacompany.webflux.request.log.RequestLogger;
import com.teliacompany.webflux.request.log.RequestLoggingOptions;
import com.teliacompany.webflux.request.log.RequestLoggingOptions.PayloadLoggingOption;
import com.teliacompany.webflux.request.log.trace.TraceLogger;
import com.teliacompany.webflux.request.metrics.DisabledMetricsReporter;
import com.teliacompany.webflux.request.metrics.MetricsReporter;
import com.teliacompany.webflux.request.utils.ByteStreamUtil;
import com.teliacompany.webflux.request.utils.Constants;
import com.teliacompany.webflux.request.utils.TransactionUtils;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder.Resilience4JCircuitBreakerConfiguration;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient.RequestBodyUriSpec;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * WebClient facade with default (but overridable) configuration and error handling based on status code. Also provides methods to send GET/POST/PUT/DELETE requests
 * and automatically retrieve them to a mono.
 */
public class WebClient {
    private static final Logger LOG = LoggerFactory.getLogger(WebClient.class);
    private static final ObjectMapper OBJECT_MAPPER = TeliaObjectMapper.get();
    public static final String REQ_WF_STARTER = "Request Webflux Starter";
    private static final RequestLoggingOptions NO_PAYLOAD_LOGGING_OPTIONS = new RequestLoggingOptions(PayloadLoggingOption.FALSE, Level.INFO, JsonPayloadLoggingFilter.empty());

    private final ErrorStatusHandler errorStatusHandler;
    private final ErrorStatusHandler.ClassToErrorMapper<?, ? extends Throwable> clientErrorConverter;
    private final HttpHeaders defaultHeaders;
    private final WebClientConfig clientConfig;
    private final Duration timeout;
    private final boolean circuitBreakerEnabled;
    private final boolean forwardAuthorization;
    private final String originalHost;
    private final WebClientConstructionPlan constructionPlan;
    private String webClientHost; //Should be synced with the host used by the realWebClient

    private RequestLogger requestLogger = new DisabledRequestLogger(); // can be overridden in setOptionalBeans
    private MetricsReporter metricsReporter = new DisabledMetricsReporter(); // can be overridden in setOptionalBeans
    private org.springframework.web.reactive.function.client.WebClient realWebClient;

    private ReactiveCircuitBreaker circuitBreaker;

    private boolean initialized = false;

    /**
     * Instantiate via builder
     */
    WebClient(WebClientConstructionPlan constructionPlan,
              HttpHeaders defaultHeaders,
              WebClientConfig clientConfig,
              ClassToErrorMapper<?, ? extends Throwable> clientErrorConverter,
              Duration timeoutDuration,
              boolean circuitBreakerEnabled,
              boolean forwardAuthorization) {
        //Store original construction plan to be able to recreate web client when needed / on reset
        this.constructionPlan = constructionPlan;
        this.realWebClient = constructionPlan.build();
        this.defaultHeaders = defaultHeaders;
        this.clientConfig = clientConfig;
        this.timeout = timeoutDuration;
        this.circuitBreakerEnabled = circuitBreakerEnabled;
        this.forwardAuthorization = forwardAuthorization;

        LOG.info("Creating client for host = {}, basePath = {}", clientConfig.getHost(), clientConfig.getBasePath());

        this.errorStatusHandler = new ErrorStatusHandler(clientConfig.getServiceName(), clientConfig.getUrl());
        this.clientErrorConverter = clientErrorConverter;
        this.originalHost = clientConfig.getHost();
        this.webClientHost = clientConfig.getHost();
    }

    /**
     * Method is called when application has loaded (app context is initialized).
     *
     * @param requestLogger   - spring bean responsible for logging
     * @param metricsReporter - spring bean responsible for reporting metrics
     */
    void init(RequestLogger requestLogger,
              MetricsReporter metricsReporter,
              ReactiveCircuitBreakerFactory<Resilience4JCircuitBreakerConfiguration, Resilience4JConfigBuilder> reactiveCircuitBreakerFactory) {
        this.requestLogger = requestLogger;
        this.metricsReporter = metricsReporter;

        reactiveCircuitBreakerFactory.configure(configBuilder -> configBuilder
                .timeLimiterConfig(TimeLimiterConfig.custom()
                        .timeoutDuration(timeout)
                        .build())
                .circuitBreakerConfig(CircuitBreakerConfig.custom()
                                .recordExceptions(GatewayTimeoutException.class, BadGatewayException.class, ServiceUnavailableException.class).build())
                .build(), clientConfig.getServiceName());

        this.circuitBreaker = reactiveCircuitBreakerFactory.create(clientConfig.getServiceName());

        this.initialized = true;
    }

    public void setHost(String newHost) {
        webClientHost = newHost;
        final String newUrl = StringUtils.removeEnd(webClientHost, "/") + "/" + StringUtils.removeStart(clientConfig.getBasePath(), "/");
        LOG.info("Setting host for client of \"{}\" to: \"{}\"", clientConfig.getServiceName(), webClientHost);

        if(newHost.contains("localhost")) {
            // Create new clientConnector for localhost, i.e. no https certificates etc
            HttpClient httpClient = HttpClient.create();
            realWebClient = realWebClient.mutate().clientConnector(new ReactorClientHttpConnector(httpClient)).baseUrl(newUrl).build();
        } else {
            realWebClient = realWebClient.mutate().baseUrl(newUrl).build();
        }
        errorStatusHandler.setEndpoint(newUrl);
    }

    public void resetBaseUrl() {
        LOG.info("Resetting webClient of \"{}\" to: \"{}\"", clientConfig.getServiceName(), webClientHost);
        webClientHost = originalHost;
        realWebClient = constructionPlan.build();
    }

    public String getServiceName() {
        return clientConfig.getServiceName();
    }

    public String getHost() {
        return clientConfig.getHost();
    }

    public boolean isProxyEnabled() {
        return clientConfig.isProxyEnabled();
    }

    public String getProxyHost() {
        return clientConfig.getProxyHost();
    }

    public Integer getProxyPort() {
        return clientConfig.getProxyPort();
    }

    public String getApiBasePath() {
        return clientConfig.getBasePath();
    }

    public RequestBuilder get(String path) {
        return new RequestBuilder(this, HttpMethod.GET, path, clientConfig.getRequestLoggingOptions());
    }

    public RequestBuilder put(String path) {
        return new RequestBuilder(this, HttpMethod.PUT, path, clientConfig.getRequestLoggingOptions());
    }

    public RequestBuilder delete(String path) {
        return new RequestBuilder(this, HttpMethod.DELETE, path, clientConfig.getRequestLoggingOptions());
    }

    public RequestBuilder post(String path) {
        return new RequestBuilder(this, HttpMethod.POST, path, clientConfig.getRequestLoggingOptions());
    }

    public RequestBuilder patch(String path) {
        return new RequestBuilder(this, HttpMethod.PATCH, path, clientConfig.getRequestLoggingOptions());
    }

    public RequestBuilder head(String path) {
        return new RequestBuilder(this, HttpMethod.HEAD, path, clientConfig.getRequestLoggingOptions());
    }

    public RequestBuilder options(String path) {
        return new RequestBuilder(this, HttpMethod.OPTIONS, path, clientConfig.getRequestLoggingOptions());
    }

    public RequestBuilder trace(String path) {
        return new RequestBuilder(this, HttpMethod.TRACE, path, clientConfig.getRequestLoggingOptions());
    }

    public static boolean isWebException(Throwable throwable) {
        return throwable instanceof WebException;
    }

    public static <T> Mono<WebClientResponse<T>> resumeOnWebException(Throwable exception, Function<WebException, T> errorHandler) {
        return resumeOnException(exception, errorHandler);
    }

    public static <X extends Throwable, T> Mono<WebClientResponse<T>> resumeOnException(Throwable exception, Function<X, T> errorHandler) {
        final HttpStatus status;
        if(exception instanceof WebException) {
            status = ((WebException) exception).getStatus();
        } else if(exception instanceof HttpStatusCodeException) {
            status = ((HttpStatusCodeException) exception).getStatusCode();
        } else {
            status = null;
        }
        return RequestProcessor.getTransactionContext()
                .map(requestContext -> {
                    T body = errorHandler.apply((X) exception);
                    return new WebClientResponse<>(body, requestContext.getRequest().getHeaders(), status);
                });
    }

    /**
     * The "Chain Definition"
     * Keep this clean, put logic in other methods, pass along the Context class.
     */
    private <T> Mono<WebClientResponse<T>> exchangeRequest(WebClientContext webClientContext, Class<T> expectedClass) {
        return RequestProcessor.getTransactionContext()
                .flatMap(transactionContext -> {
                    // Note: There are three types of contexts at this point: transactionContext (set up by RequestProcessor), requestContext (set up below) and the
                    // WebClientContext (the context passed through the mono chain below). requestContext and WebClientContext could possibly be merged, but that would
                    // result in less beautiful chaining below basically because you cant map on empty monos for some fu**ing reason. Ideally everything would be in the
                    // request context and some steps below should return Mono<Void> or null, but then these can't be mapped so it sux. Maybe there's an alternative to
                    // map when monos return null similar to CompletableFuture.thenAccept() but I haven't found it...
                    final HttpHeaders headers = new HttpHeaders(webClientContext.headers);
                    headers.addAll(defaultHeaders);

                    if(forwardAuthorization) {
                        transactionContext.getRequest().getHeader("Authorization")
                                .ifPresent(authHeaderValue -> headers.add("Authorization", authHeaderValue));
                    }

                    final RequestContext requestContext = new RequestContextBuilder()
                            .withTransactionContext(transactionContext)
                            .withCookies(webClientContext.cookies)
                            .withHeaders(headers)
                            .withHttpMethod(webClientContext.method)
                            .withUriVariables(webClientContext.uriVariables)
                            .withUri(webClientHost, clientConfig.getBasePath() + webClientContext.path)
                            .withServiceName(clientConfig.getServiceName())
                            .buildRequestContext();

                    return createRequestSpec(webClientContext)
                            .map(this::setUri)
                            .flatMap(this::setRequestHeaders)
                            .map(this::setRequestCookies)
                            .map(this::convertAndSetPayload)
                            .flatMap(this::logRequest)
                            .map(this::clearRequestInContext)
                            .flatMap(this::exchange)
                            .flatMap(this::recordRequestDuration)
                            .flatMap(this::setMetaData)
                            .flatMap(this::onErrorStatus)
                            .flatMap(this::extractPayload)
                            .flatMap(this::logResponse)
                            .map(ctx -> parseResponse(expectedClass, ctx))
                            .contextWrite(ctx -> ctx.put(Constants.REQUEST_CONTEXT_KEY, requestContext));
                });
    }

    private Mono<WebClientContext> createRequestSpec(WebClientContext wcRequestContext) {
        final RequestBodyUriSpec requestSpec = realWebClient.method(wcRequestContext.method);
        return Mono.just(wcRequestContext).map(context -> context.setRequestSpec(requestSpec));
    }

    private WebClientContext setUri(WebClientContext wcRequestContext) {
        if (!wcRequestContext.queryParams.isEmpty()) {
            wcRequestContext.requestSpec.uri(uriBuilder -> uriBuilder.path(wcRequestContext.path)
                    .queryParams(wcRequestContext.queryParams).build(wcRequestContext.uriVariables));
        } else if(!wcRequestContext.uriVariables.isEmpty()) {
            //Some projects use uriVariables to add also query parameters to the request. For them this way is needed as
            //otherwise the builder will do url encoding
            wcRequestContext.requestSpec.uri(wcRequestContext.path, wcRequestContext.uriVariables);
        } else {
            //Not sure what happens if we send empty uriVariables / if it differs from not providing them at all like below.
            wcRequestContext.requestSpec.uri(wcRequestContext.path);
        }
        
        return wcRequestContext;
    }

    private Mono<WebClientContext> setRequestHeaders(WebClientContext webClientRequestContext) {
        return RequestProcessor.getTransactionContext()
                .map(transactionContext -> {
                    final List<String> tidList = Collections.singletonList(transactionContext.getTid());
                    //Note: When headers are logged these are extracted from webClientRequestContext.headers and not requestSpec
                    webClientRequestContext.headers.putIfAbsent(Constants.HTTP_X_TCAD, Collections.singletonList(transactionContext.getTcad()));
                    webClientRequestContext.headers.putIfAbsent(Constants.HTTP_X_TSCID, Collections.singletonList(transactionContext.getTscid()));
                    webClientRequestContext.headers.putIfAbsent(Constants.HTTP_REQUEST_ID_HEADER, tidList);
                    webClientRequestContext.headers.putIfAbsent(Constants.HTTP_CORRELATION_ID_HEADER, tidList);
                    webClientRequestContext.headers.putIfAbsent(Constants.HTTP_TRANSACTION_ID_HEADER, tidList);
                    webClientRequestContext.headers.putIfAbsent(Constants.HTTP_TRANSACTION_META_DATA_HEADER, TransactionUtils.getMetaDataAsHeaderValue(transactionContext));
                    webClientRequestContext.headers.forEach((headerKey, headerValues) -> webClientRequestContext.requestSpec.header(headerKey, headerValues.toArray(new String[0])));
                    return webClientRequestContext;
                });
    }

    private WebClientContext setRequestCookies(final WebClientContext webClientRequestContext) {
        webClientRequestContext.cookies.forEach((cookieKey, cookieValues) -> cookieValues.forEach(cookieValue -> webClientRequestContext.requestSpec.cookie(cookieKey, cookieValue)));
        return webClientRequestContext;
    }

    private WebClientContext convertAndSetPayload(WebClientContext wcContext) {
        if(wcContext.requestBody != null) {
            String requestPayload;
            if(wcContext.requestBody instanceof String) {
                requestPayload = (String) wcContext.requestBody;
            } else {
                try {
                    requestPayload = OBJECT_MAPPER.writeValueAsString(wcContext.requestBody);
                } catch(JsonProcessingException e) {
                    LOG.error("Could not construct response body!");
                    throw new InternalServerErrorException("Could not write json response", REQ_WF_STARTER, e);
                }
            }
            wcContext.setRequestPayload(requestPayload);
            if(wcContext.headers != null &&
                    MediaType.MULTIPART_FORM_DATA.equals(wcContext.headers.getContentType()) &&
                    wcContext.requestBody instanceof MultiValueMap){
                wcContext.requestSpec.body(BodyInserters.fromMultipartData((MultiValueMap<String, ?>) wcContext.requestBody));
            }else{
                wcContext.requestSpec.body(BodyInserters.fromValue(requestPayload));
            }        }
        return wcContext;
    }

    private Mono<WebClientContext> logRequest(WebClientContext webClientContext) {
        return RequestProcessor.getRequestContext()
                .map(requestContext -> {
                    requestLogger.logRequest(requestContext, webClientContext.requestPayload, webClientContext.requestLoggingOptions);
                    return webClientContext;
                })
                .flatMap(TraceLogger.log("Calling {} on {}{}{} using {}", clientConfig.getServiceName(),
                        clientConfig.getHost(), clientConfig.getBasePath(), webClientContext.path,
                        webClientContext.method));
    }

    /**
     * Free up some memory by removing no longer used request data from the context. If response takes a while it will take as much time before this
     * whole context is garbage collected. As soon as the request is performed (exchange()) the request payload and object is no longer accessed and can be cleared.
     * Call this just before this.exchange(WebClientContext wcContext)
     */
    private WebClientContext clearRequestInContext(WebClientContext webClientContext) {
        return webClientContext.setRequestBody(null)
                .setRequestPayload(null);
    }

    private Mono<WebClientContext> exchange(WebClientContext wcContext) {
        // (Remove comment when fixed!)
        // They have changed this now and added exchangeToMono() to better handle memory leaks (Finally). We have already solved this by making sure to consume the body.
        // However, this is not as easy as changing to:
        // Mono<WebClientContext> exchange = wcContext.requestSpec.exchangeToMono(response -> Mono.just(wcContext.setResponse(response)))
        // Since this will create a "free floating" mono or something... It could be that we need to rewrite quite a bit for this...
        Mono<WebClientContext> exchange = wcContext.requestSpec.exchange()
                .map(wcContext::setResponse);

        wcContext.requestSpec = null; // No need to hold on to this anymore, it can potentially hold a big payload.
        if(circuitBreakerEnabled) {
            return circuitBreaker.run(exchange, throwable -> {
                LOG.debug("circuitBreaker fallback");
                return exchange.timeout(Duration.ZERO)
                        .onErrorResume(fakeTimeoutError -> {
                            LOG.debug("Service unavailable", throwable);
                            return setMetaData(wcContext)
                                    .flatMap(webClientContext -> RequestProcessor.getRequestContext())
                                    .flatMap(requestContext -> {
                                        requestContext.recordRequestDuration();
                                        final ClientResponse dummyResponse = ClientResponse.create(HttpStatus.SERVICE_UNAVAILABLE).build();
                                        requestLogger.logResponse(requestContext, dummyResponse, null, NO_PAYLOAD_LOGGING_OPTIONS);
                                        return Mono.error(new ServiceUnavailableException("Service Unavailable", getServiceName(), throwable));
                                    });
                        });
            });
        } else {
            return exchange;
        }
    }

    private Mono<WebClientContext> recordRequestDuration(WebClientContext context) {
        return RequestProcessor.getRequestContext()
                .map(requestContext -> {
                    requestContext.recordRequestDuration();
                    return context;
                });
    }

    private Mono<WebClientContext> setMetaData(WebClientContext context) {
        return RequestProcessor.getTransactionContext()
                .map(transactionContext -> {
                    if(context.response != null) {
                        final List<String> incomingMetaData = context.response.headers().header(Constants.HTTP_TRANSACTION_META_DATA_HEADER);
                        TransactionUtils.addMetaDataFromHeaders(incomingMetaData, transactionContext.getMetaData());

                        final List<String> incomingSubSystemTIDs = new ArrayList<>();
                        incomingSubSystemTIDs.addAll(context.response.headers().header(Constants.HTTP_REQUEST_ID_HEADER));
                        incomingSubSystemTIDs.addAll(context.response.headers().header(Constants.HTTP_CORRELATION_ID_HEADER));
                        incomingSubSystemTIDs.addAll(context.response.headers().header(Constants.HTTP_TRANSACTION_ID_HEADER));
                        if(!incomingSubSystemTIDs.isEmpty()) {
                            String tidString = String.join(" | ", incomingSubSystemTIDs);
                            transactionContext.getMetaData().putIfAbsent(Constants.SUB_SYSTEM_TID, tidString);
                        }
                    }
                    return context;
                });
    }

    private Mono<WebClientContext> onErrorStatus(WebClientContext wcContext) {
        ClientResponse clientResponse = wcContext.response;
        return RequestProcessor.getRequestContext()
                .flatMap(requestContext -> {
                    if(!clientResponse.statusCode().is2xxSuccessful()) {
                        metricsReporter.reportRequestError(requestContext, clientResponse);
                        if(wcContext.requestErrorConverter != null) {
                            return errorStatusHandler.handleErrorWithExtractor(clientResponse, wcContext.requestErrorConverter, requestLogger, wcContext.requestLoggingOptions).map(wcContext::setResponse);
                        } else if(clientErrorConverter != null) {
                            return errorStatusHandler.handleErrorWithExtractor(clientResponse, clientErrorConverter, requestLogger, wcContext.requestLoggingOptions).map(wcContext::setResponse);
                        } else {
                            return errorStatusHandler.handleDefault(clientResponse, requestLogger, wcContext.requestLoggingOptions).map(wcContext::setResponse);
                        }
                    }
                    return Mono.just(wcContext);
                });
    }

    private Mono<WebClientContext> extractPayload(WebClientContext webClientContext) {
        final Mono<byte[]> bodyMono;
        ClientResponse clientResponse = webClientContext.response;
        if(HttpStatus.NO_CONTENT.equals(clientResponse.statusCode())) {
            bodyMono = clientResponse.releaseBody() //need to consume body even though it is empty
                    .map(v -> new byte[0]) //This will never happen, just need to define what kind of object the switchIfEmpty shall return below
                    .switchIfEmpty(Mono.just(new byte[0]));
        } else {
            // To support large responses, read body to flux of data buffers (chunks of data), collect them into a byte[] using a ByteArrayOutputStream.
            bodyMono = clientResponse.bodyToFlux(DataBuffer.class)
                    .map(dataBuffer -> {
                        ByteBuffer byteBuffer = dataBuffer.asByteBuffer();
                        byte[] byteArray = new byte[byteBuffer.remaining()];
                        byteBuffer.get(byteArray);
                        DataBufferUtils.release(dataBuffer);
                        return byteArray;
                    })
                    .reduce(new ByteArrayOutputStream(), ByteStreamUtil::writeBytesToStream)
                    .map(ByteArrayOutputStream::toByteArray)
                    .onErrorResume(t -> releaseBody(clientResponse, new byte[0])
                            .flatMap(b -> Mono.error(new InternalServerErrorException("Could not read body", REQ_WF_STARTER, t))))
                    .flatMap(o -> releaseBody(clientResponse, o))
                    .switchIfEmpty(Mono.defer(() -> {
                        LOG.info("Got empty body in OK response, releasing body...");
                        return releaseBody(clientResponse, new byte[0]);
                    }));
        }
        return bodyMono.map(webClientContext::setResponsePayload)
                .switchIfEmpty(Mono.defer(() -> Mono.just(webClientContext)));
    }

    private <T> Mono<T> releaseBody(ClientResponse clientResponse, T o) {
        return clientResponse.releaseBody()
                .map(v -> o)
                .switchIfEmpty(Mono.just(o));
    }

    private Mono<WebClientContext> logResponse(WebClientContext webClientContext) {
        return RequestProcessor.getRequestContext()
                .map(requestContext -> {
                    requestLogger.logResponse(requestContext, webClientContext.response, webClientContext.responsePayload, webClientContext.requestLoggingOptions);
                    metricsReporter.reportRequestData(requestContext, webClientContext.response.statusCode());
                    return webClientContext;
                })
                .flatMap(TraceLogger.log("Received response from {}", clientConfig.getServiceName()));

    }

    @SuppressWarnings("unchecked")
    private static <T> WebClientResponse<T> parseResponse(Class<T> expectedClass, WebClientContext webClientContext) {
        final HttpHeaders headers = webClientContext.response.headers().asHttpHeaders();
        final HttpStatus statusCode = webClientContext.response.statusCode();
        final T responseObject;

        if(webClientContext.responsePayload.length == 0 || expectedClass.equals(Void.class)) {
            responseObject = null;
        } else if(expectedClass.equals(byte[].class)) {
            //If a byte[] is expected, just return it...
            responseObject = (T) webClientContext.responsePayload;
        } else if(expectedClass.equals(String.class)) {
            //If a string is expected, dont try to parse it as a json to a string...
            responseObject = (T) new String(webClientContext.responsePayload, StandardCharsets.UTF_8);
        } else {
            try {
                responseObject = TeliaObjectMapper.get().readValue(webClientContext.responsePayload, expectedClass);
                webClientContext.responsePayload = new byte[0]; // Make sure we don't hold on to this.
            } catch(IOException e) {
                throw new InternalServerErrorException("Could not convert from json", "Jackson", e);
            }
        }
        return new WebClientResponse<>(responseObject, headers, statusCode);
    }

    public boolean isInitialized() {
        return initialized;
    }

    public static class RequestBuilder {
        private final WebClient webClient;
        private final WebClientContext webClientRequestContext;

        private RequestBuilder(WebClient webClient, HttpMethod method, String path, RequestLoggingOptions requestLoggingOptions) {
            this.webClient = webClient;
            this.webClientRequestContext = new WebClientContext(method, path, requestLoggingOptions);
        }

        public RequestBuilder body(Object requestBody) {
            this.webClientRequestContext.setRequestBody(requestBody);
            return this;
        }

        public RequestBuilder header(String key, String... values) {
            this.webClientRequestContext.headers.put(key, Arrays.asList(values));
            return this;
        }

        public RequestBuilder cookie(String key, String value) {
            this.webClientRequestContext.cookies.computeIfAbsent(key, k -> new ArrayList<>());
            this.webClientRequestContext.cookies.get(key).add(value);
            return this;
        }

        public RequestBuilder uriVariable(String key, String value) {
            this.webClientRequestContext.uriVariables.put(key, value);
            return this;
        }

        public RequestBuilder queryParam(String key, String value) {
            this.webClientRequestContext.queryParams.add(key, value);
            return this;
        }

        public RequestBuilder disableRequestPayloadLogging() {
            this.webClientRequestContext.requestLoggingOptions.setRequestPayloadLoggingOption(PayloadLoggingOption.FALSE);
            return this;
        }

        public RequestBuilder disableResponsePayloadLogging() {
            this.webClientRequestContext.requestLoggingOptions.setResponsePayloadLoggingOption(PayloadLoggingOption.FALSE);
            return this;
        }

        public RequestBuilder disableLogging() {
            this.webClientRequestContext.requestLoggingOptions.disableLogging();
            return this;
        }

        /**
         * Overrides the default and optionally set error converter on client level.
         * Convert error class to error string message.
         * Exception is automatically created based on http status code.
         *
         * @param clazz     - class to map root cause error to
         * @param converter - mapping function to map the error -> string
         */
        public <E, X extends Throwable> RequestBuilder errorConverter(Class<E> clazz, Function<WebClientResponse<E>, X> converter) {
            this.webClientRequestContext.requestErrorConverter = new ErrorStatusHandler.ClassToErrorMapper<>(clazz, converter);
            return this;
        }

        public Mono<WebClientResponse<Void>> retrieve() {
            return retrieve(Void.class);
        }

        public <T> Mono<WebClientResponse<T>> retrieve(Class<T> expectedClass) {
            return webClient.exchangeRequest(webClientRequestContext, expectedClass);
        }
    }

    private static class WebClientContext {
        private final HttpMethod method;
        private final String path;
        private final HttpHeaders headers = new HttpHeaders();
        private final Map<String, List<String>> cookies = new HashMap<>();
        private final Map<String, String> uriVariables = new HashMap<>();
        private final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        private final RequestLoggingOptions requestLoggingOptions;
        private Object requestBody;
        private RequestBodyUriSpec requestSpec;
        private String requestPayload;
        private ClientResponse response;
        private byte[] responsePayload = new byte[0];
        private ErrorStatusHandler.ClassToErrorMapper<?, ? extends Throwable> requestErrorConverter;

        WebClientContext(HttpMethod method, String path, RequestLoggingOptions requestLoggingOptions) {
            this.method = method;
            if(!path.isEmpty()) {
                this.path = "/" + StringUtils.removeStart(path, "/");
            } else {
                this.path = path;
            }
            this.requestLoggingOptions = requestLoggingOptions;
        }

        WebClientContext setRequestSpec(RequestBodyUriSpec requestSpec) {
            this.requestSpec = requestSpec;
            return this;
        }

        WebClientContext setRequestPayload(String requestPayload) {
            this.requestPayload = requestPayload;
            return this;
        }

        WebClientContext setRequestBody(Object requestBody) {
            this.requestBody = requestBody;
            return this;
        }

        WebClientContext setResponse(ClientResponse response) {
            this.response = response;
            return this;
        }

        WebClientContext setResponsePayload(byte[] responsePayload) {
            this.responsePayload = responsePayload;
            return this;
        }
    }
}
