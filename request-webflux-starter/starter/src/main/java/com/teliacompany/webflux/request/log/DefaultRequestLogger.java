package com.teliacompany.webflux.request.log;

import com.teliacompany.webflux.request.config.LoggingConfig;
import com.teliacompany.webflux.request.context.ContextHttpRequest;
import com.teliacompany.webflux.request.context.RequestContext;
import com.teliacompany.webflux.request.context.TransactionContext;
import com.teliacompany.webflux.request.context.TransactionResponse;
import com.teliacompany.webflux.error.ErrorAttributesUtils;
import com.teliacompany.webflux.error.exception.WebException;
import com.teliacompany.webflux.request.log.RequestLoggingOptions.PayloadLoggingOption;
import com.teliacompany.webflux.request.utils.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.reactive.function.client.ClientResponse;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static com.teliacompany.webflux.request.utils.Constants.ADDRESS;
import static com.teliacompany.webflux.request.utils.Constants.CONTENT_TYPE;
import static com.teliacompany.webflux.request.utils.Constants.COOKIES;
import static com.teliacompany.webflux.request.utils.Constants.DIRECTION;
import static com.teliacompany.webflux.request.utils.Constants.HEADERS;
import static com.teliacompany.webflux.request.utils.Constants.HOST;
import static com.teliacompany.webflux.request.utils.Constants.HTTP_METHOD;
import static com.teliacompany.webflux.request.utils.Constants.INBOUND;
import static com.teliacompany.webflux.request.utils.Constants.OUTBOUND;
import static com.teliacompany.webflux.request.utils.Constants.PAYLOAD;
import static com.teliacompany.webflux.request.utils.Constants.PAYLOAD_INFO;
import static com.teliacompany.webflux.request.utils.Constants.PAYLOAD_LENGTH;
import static com.teliacompany.webflux.request.utils.Constants.REQUEST_DURATION;
import static com.teliacompany.webflux.request.utils.Constants.REQUEST_ID;
import static com.teliacompany.webflux.request.utils.Constants.RESPONSE;
import static com.teliacompany.webflux.request.utils.Constants.RESPONSE_CODE;
import static com.teliacompany.webflux.request.utils.Constants.SUB_SERVICE_NAME;
import static com.teliacompany.webflux.request.utils.Constants.TYPE;
import static com.teliacompany.webflux.request.utils.Constants.URI_VARIABLES;

public class DefaultRequestLogger implements RequestLogger {
    private static final Logger LOG = LogManager.getLogger(DefaultRequestLogger.class);
    public static final String PAYLOAD_LOGGING_DISABLED = "***payload logging disabled***";
    private final LoggingConfig loggingConfig;
    private final String applicationName;

    public DefaultRequestLogger(LoggingConfig loggingConfig, String applicationName) {
        this.loggingConfig = loggingConfig;
        this.applicationName = applicationName;
    }

    @Override
    public void logResponse(RequestContext requestContext, ClientResponse clientResponse, Object payload, RequestLoggingOptions loggingOptions) {
        final HttpHeaders httpHeaders = clientResponse.headers().asHttpHeaders();
        getResponseMessage(requestContext, payload, clientResponse.statusCode(), httpHeaders, loggingOptions)
                .ifPresent(map -> logMessage(loggingOptions, map));
    }

    @Override
    public void logResponse(RequestContext requestContext, TransactionResponse transactionResponse, RequestLoggingOptions loggingOptions) {
        getResponseMessage(requestContext, transactionResponse.getBody(), transactionResponse.getHttpStatus(), transactionResponse.getHttpHeaders(), loggingOptions)
                .ifPresent(map -> logMessage(loggingOptions, map));
    }

    @Override
    public void logError(RequestContext requestContext, Throwable e, RequestLoggingOptions loggingOptions) {
        final HttpHeaders headers = requestContext.getRequest().getHeaders();
        final HttpStatus responseCode;
        final String payloadJson;
        if(e instanceof WebException) {
            WebException webException = (WebException) e;
            responseCode = webException.getStatus();
            payloadJson = LoggerUtils.toJsonMessage(ErrorAttributesUtils.buildFromWebException(webException, applicationName), false);
        } else if(e instanceof HttpStatusCodeException) {
            HttpStatusCodeException httpStatusCodeException = (HttpStatusCodeException) e;
            responseCode = httpStatusCodeException.getStatusCode();
            payloadJson = LoggerUtils.toJsonMessage(ErrorAttributesUtils.buildFromGenericException(httpStatusCodeException, responseCode, applicationName), false);
        } else {
            responseCode = HttpStatus.INTERNAL_SERVER_ERROR;
            payloadJson = LoggerUtils.toJsonMessage(ErrorAttributesUtils.buildFromGenericException(e, responseCode, applicationName), false);
        }

        getResponseMessage(requestContext, payloadJson, responseCode, headers, loggingOptions)
                .ifPresent(map -> logMessage(loggingOptions, map));
    }

    @Override
    public void logRequest(RequestContext context, byte[] payload, RequestLoggingOptions loggingOptions) {
        getRequestMessage(context, payload, loggingOptions)
                .ifPresent(map -> logMessage(loggingOptions, map));
    }

    @Override
    public void logRequest(RequestContext context, String payload, RequestLoggingOptions loggingOptions) {
        if(payload == null) {
            logRequest(context, (byte[]) null, loggingOptions);
        } else {
            logRequest(context, payload.getBytes(StandardCharsets.UTF_8), loggingOptions);
        }
    }

    @Override
    public void logInternalRequest(RequestContext context, RequestLoggingOptions loggingOptions) {
        logMessage(loggingOptions, getInternalRequestMessage(context));
    }

    @Override
    public void logMessage(RequestLoggingOptions loggingOptions, Map<String, Object> map) {
        if (loggingOptions.isLoggingEnabled()) {
            if (loggingConfig.isLogAsObjectMessage()) {
                LOG.log(loggingOptions.getLogLevel(), LoggerUtils.toJsonMessage(map, true));
            } else {
                LOG.log(loggingOptions.getLogLevel(), LoggerUtils.toStringMessage(map));
            }
        }
    }

    Optional<Map<String, Object>> getResponseMessage(RequestContext requestContext, Object payload, HttpStatus responseCode, HttpHeaders httpHeaders, RequestLoggingOptions requestLoggingOptions) {
        if(!isAddressOkToLog(requestContext.getRequest().getAddress()) || !isResponseCodeOkToLog(responseCode)) {
            return Optional.empty();
        }
        final String direction = requestContext instanceof TransactionContext ? OUTBOUND : INBOUND;
        final String requestId = requestContext.getRequestId();
        final Long requestDuration = requestContext.getRequestDuration();

        Map<String, Object> map = new LinkedHashMap<>();
        putIfNotNull(map, REQUEST_ID, requestId);
        putIfNotNull(map, DIRECTION, direction);
        putIfNotNull(map, TYPE, RESPONSE);
        putIfNotNull(map, REQUEST_DURATION, requestDuration);
        putIfNotNull(map, SUB_SERVICE_NAME, requestContext.getServiceName());
        LoggerUtils.getContentType(httpHeaders).ifPresent(contentType -> map.put(CONTENT_TYPE, contentType));
        Optional.ofNullable(responseCode).ifPresent(rc -> map.put(RESPONSE_CODE, rc.value()));

        //Headers
        final HttpHeaders filteredHeaders = loggingConfig.getHeadersFilter().filterHeaders(httpHeaders);
        if(loggingConfig.isEncodeHeaders()) {
            map.put(HEADERS, LoggerUtils.base64EncodeHeaders(filteredHeaders));
        } else {
            map.put(HEADERS, filteredHeaders);
        }

        //Payload
        if(payload instanceof InputStreamResource) {
            map.put(PAYLOAD, "Binary");
        } else {
            PayloadLog payloadLog = null;
            if(payload instanceof byte[]) {
                payloadLog = getPayloadMessage((byte[]) payload, requestLoggingOptions);
            } else if(payload instanceof String) {
                payloadLog = getPayloadMessage((String) payload, requestLoggingOptions);
            }
            if(payloadLog != null) {
                map.put(PAYLOAD_LENGTH, payloadLog.length);
                map.put(PAYLOAD, payloadLog.payload);
                map.put(PAYLOAD_INFO, payloadLog.info);
            }
        }
        map.putAll(requestContext.getMetaData());
        return Optional.of(map);
    }

    Optional<Map<String, Object>> getRequestMessage(RequestContext context, byte[] payload, RequestLoggingOptions requestLoggingOptions) {
        final ContextHttpRequest request = context.getRequest();
        if(!isAddressOkToLog(request.getAddress())) {
            return Optional.empty();
        }
        final String direction = context instanceof TransactionContext ? INBOUND : OUTBOUND;

        Map<String, Object> map = new LinkedHashMap<>();
        putIfNotNull(map, REQUEST_ID, context.getRequestId());
        putIfNotNull(map, DIRECTION, direction);
        putIfNotNull(map, TYPE, Constants.REQUEST);
        putIfNotNull(map, HOST, request.getHost());
        putIfNotNull(map, ADDRESS, request.getAddress());
        putIfNotNull(map, HTTP_METHOD, request.getHttpMethod(), HttpMethod::name);
        putIfNotNull(map, SUB_SERVICE_NAME, context.getServiceName());

        if(request.getUriVariables() != null && !request.getUriVariables().isEmpty()) {
            map.put(URI_VARIABLES, request.getUriVariables());
        }

        LoggerUtils.getContentType(request.getHeaders()).ifPresent(contentType -> map.put(CONTENT_TYPE, contentType));
        final HttpHeaders httpHeaders = loggingConfig.getHeadersFilter().filterHeaders(request.getHeaders());
        if(loggingConfig.isEncodeHeaders()) {
            map.put(HEADERS, LoggerUtils.base64EncodeHeaders(httpHeaders));
        } else {
            map.put(HEADERS, httpHeaders);
        }

        if(request.getCookies() != null && !request.getCookies().isEmpty()) {
            map.put(COOKIES, request.getCookies());
        }

        if(payload != null) {
            final PayloadLog payloadLog = getPayloadMessage(payload, requestLoggingOptions);
            map.put(PAYLOAD_LENGTH, payloadLog.length);
            map.put(PAYLOAD, payloadLog.payload);
        }
        map.putAll(context.getMetaData());

        return Optional.of(map);
    }

    Map<String, Object> getInternalRequestMessage(RequestContext context) {
        Map<String, Object> map = new LinkedHashMap<>();
        putIfNotNull(map, DIRECTION, Constants.INTERNAL);
        putIfNotNull(map, TYPE, Constants.INTERNAL);
        map.putAll(context.getMetaData());
        return map;
    }

    private static void putIfNotNull(Map<String, Object> map, String key, Object value) {
        if(value != null) {
            map.put(key, value);
        }
    }

    private static <T> void putIfNotNull(Map<String, Object> map, String key, T value, Function<T, String> valueExtractor) {
        if(value != null) {
            String subValue = valueExtractor.apply(value);
            putIfNotNull(map, key, subValue);
        }
    }

    private boolean shouldLogPayload(PayloadLoggingOption payloadLoggingOption) {
        switch(payloadLoggingOption) {
            case TRUE:
                return true;
            case FALSE:
                return false;
            default:
            case DEFAULT:
                return loggingConfig.isRequestPayloadLoggingEnabled();
        }
    }

    private PayloadLog getPayloadMessage(String stringPayload, RequestLoggingOptions requestLoggingOptions) {
        if(!shouldLogPayload(requestLoggingOptions.getRequestPayloadLoggingOption())) {
            return PayloadLog.of("", PAYLOAD_LOGGING_DISABLED, 0);
        }
        return getPayloadMessage(stringPayload.getBytes(StandardCharsets.UTF_8), requestLoggingOptions);
    }

    private PayloadLog getPayloadMessage(byte[] bytePayload, RequestLoggingOptions requestLoggingOptions) {
        boolean payloadLoggingEnabled = shouldLogPayload(requestLoggingOptions.getRequestPayloadLoggingOption());
        if(!payloadLoggingEnabled) {
            return PayloadLog.of("", PAYLOAD_LOGGING_DISABLED, 0);
        }

        final int maxEncodedLength = loggingConfig.getMaxEncodedPayloadLoggingLength() / 2; // Every char is 2 bytes!
        String payloadMetaMessage = bytePayload.length > maxEncodedLength ? "Payload to large to be fully logged" : null;
        //Make sure we are working on a copy of the payload with a max size...
        int max = Math.min(bytePayload.length, maxEncodedLength);
        byte[] bytesToBeLogged = Arrays.copyOfRange(bytePayload, 0, max);

        if(loggingConfig.isEncodePayload() || bytesToBeLogged.length >= loggingConfig.getMaxPayloadLoggingLength()) {
            return PayloadLog.of(Base64.getEncoder().encodeToString(requestLoggingOptions.getPayloadLoggingFilter().filterPayload(bytesToBeLogged)), payloadMetaMessage, bytePayload.length);
        } else {
            for(int i = 0; i < bytesToBeLogged.length; i++) {
                if(bytesToBeLogged[i] == '\n' || bytesToBeLogged[i] == '\r') {
                    bytesToBeLogged[i] = ' ';
                }
            }
            return PayloadLog.of(requestLoggingOptions.getPayloadLoggingFilter().filterPayload(new String(bytesToBeLogged, StandardCharsets.UTF_8)), payloadMetaMessage, bytePayload.length);
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isAddressOkToLog(String address) {
        if(address == null) {
            return true;
        }
        if(!loggingConfig.getIncludePaths().isEmpty()) {
            return loggingConfig.getIncludePaths().stream().anyMatch(address::startsWith);
        } else {
            return loggingConfig.getIgnorePaths().stream().noneMatch(address::startsWith);
        }
    }

    private boolean isResponseCodeOkToLog(HttpStatus httpStatus) {
        if(httpStatus == null) {
            return true;
        }
        return loggingConfig.getIgnoreResponseCodes().stream().noneMatch(responseCode -> httpStatus.value() == responseCode);
    }

    private static class PayloadLog {
        private final String payload;
        private final String info;
        private final int length;

        private PayloadLog(String payload, String info, int length) {
            this.payload = payload;
            this.info = info;
            this.length = length;
        }

        private static PayloadLog of(String payload, String info, int length) {
            return new PayloadLog(payload, info, length);
        }
    }
}
