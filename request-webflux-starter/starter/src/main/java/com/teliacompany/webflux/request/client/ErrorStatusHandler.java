package com.teliacompany.webflux.request.client;

import com.teliacompany.webflux.request.context.RequestContext;
import com.teliacompany.webflux.error.api.ErrorCause;
import com.teliacompany.webflux.error.exception.WebException;
import com.teliacompany.webflux.jackson.TeliaObjectMapper;
import com.teliacompany.webflux.request.RequestProcessor;
import com.teliacompany.webflux.request.log.RequestLogger;
import com.teliacompany.webflux.request.log.RequestLoggingOptions;
import com.teliacompany.webflux.request.utils.TransactionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Locale;
import java.util.function.Function;

@SuppressWarnings("unchecked")
final class ErrorStatusHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ErrorStatusHandler.class);
    private static final String DEFAULT_CLIENT_ERROR_MESSAGE = "Got client error response when requesting %s on endpoint %s.";
    private static final String DEFAULT_SERVER_ERROR_MESSAGE = "Got server error response when requesting %s on endpoint %s.";
    public static final String TRACE_ERROR_TEMPLATE = "Received error response from {} with status code: {}";

    private final String service;
    private String endpoint;

    ErrorStatusHandler(String service, String endpoint) {
        this.service = service;
        this.endpoint = endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    @SuppressWarnings("DuplicatedCode")
    Mono<ClientResponse> handleDefault(ClientResponse clientResponse, RequestLogger requestLogger, RequestLoggingOptions requestLoggingOptions) {
        return clientResponse
                .bodyToMono(String.class)
                .defaultIfEmpty("N/A")
                .flatMap(payload -> RequestProcessor.getContext()
                        .map(subscriberContext -> {
                            final RequestContext requestContext = subscriberContext.getRequestContext();
                            requestLogger.logResponse(requestContext, clientResponse, payload, requestLoggingOptions);
                            subscriberContext.getTraceLogger().addLogEntry(TRACE_ERROR_TEMPLATE, requestContext.getServiceName(), clientResponse.statusCode());
                            return payload;
                        }))
                .flatMap(causeMessage -> {
                    WebException exception = getDefaultWebException(clientResponse, causeMessage);
                    return Mono.error(exception);
                });
    }

    /**
     * Use this when custom error handling is needed
     * Tells the client to first map the error to a specific class. Then it applies your mapping function that should map it to a string (error message)
     */
    @SuppressWarnings("DuplicatedCode")
    <T, E extends Throwable> Mono<ClientResponse> handleErrorWithExtractor(ClientResponse clientResponse, ClassToErrorMapper<T, E> classToErrorMapper, RequestLogger requestLogger, RequestLoggingOptions requestLoggingOptions) {
        return clientResponse
                .bodyToMono(String.class)
                .defaultIfEmpty("N/A")
                .flatMap(payload -> RequestProcessor.getContext()
                        .map(subscriberContext -> {
                            final RequestContext requestContext = subscriberContext.getRequestContext();
                            requestLogger.logResponse(requestContext, clientResponse, payload, requestLoggingOptions);
                            subscriberContext.getTraceLogger().addLogEntry(TRACE_ERROR_TEMPLATE, requestContext.getServiceName(), clientResponse.statusCode());
                            return payload;
                        }))
                .map(payload -> {
                    if(classToErrorMapper.getClazz().equals(String.class)) {
                        return (T) payload;
                    }
                    try {
                        return TeliaObjectMapper.get().readValue(payload, classToErrorMapper.getClazz());
                    } catch(IOException e) {
                        LOG.error("Could not map error to specified class. Setting empty payload", e);
                        throw getDefaultWebException(clientResponse, payload);
                    }
                })
                .map(payload -> {
                    WebClientResponse<T> response = new WebClientResponse<>(payload, clientResponse.headers().asHttpHeaders(), clientResponse.statusCode());
                    return classToErrorMapper.getErrorHandler().apply(response);
                })
                .flatMap(Mono::error);
    }

    private WebException getDefaultWebException(ClientResponse clientResponse, String payload) {
        final String messageTemplate = clientResponse.statusCode().is4xxClientError() ? DEFAULT_CLIENT_ERROR_MESSAGE : DEFAULT_SERVER_ERROR_MESSAGE;
        final String message = String.format(Locale.ROOT, messageTemplate, service, endpoint);
        final String causeTidHeader = TransactionUtils.getTransactionIdFromHeaders(clientResponse.headers().asHttpHeaders(), () -> null);

        return WebException.fromHttpStatus(clientResponse.statusCode(), ErrorCause.from(service, payload), message)
                .setCauseTransactionId(causeTidHeader)
                .setCauseHttpStatus(clientResponse.statusCode().value());
    }

    static class ClassToErrorMapper<T, E extends Throwable> {
        private final Class<T> clazz;
        private final Function<WebClientResponse<T>, E> mapper;

        ClassToErrorMapper(Class<T> clazz, Function<WebClientResponse<T>, E> errorHandler) {
            this.clazz = clazz;
            this.mapper = errorHandler;
        }

        Class<T> getClazz() {
            return clazz;
        }

        Function<WebClientResponse<T>, E> getErrorHandler() {
            return mapper;
        }
    }

}
