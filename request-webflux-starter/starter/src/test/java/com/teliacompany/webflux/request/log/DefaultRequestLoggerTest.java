package com.teliacompany.webflux.request.log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.teliacompany.webflux.request.config.LoggingConfig;
import com.teliacompany.webflux.request.context.RequestContext;
import com.teliacompany.webflux.request.context.RequestContextBuilder;
import com.teliacompany.webflux.request.context.TransactionContext;
import com.teliacompany.webflux.request.log.RequestLoggingOptions.PayloadLoggingOption;
import com.teliacompany.webflux.request.utils.Constants;
import com.teliacompany.webflux.jackson.TeliaObjectMapper;
import org.apache.logging.log4j.Level;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class DefaultRequestLoggerTest {
    private LoggingConfig loggingConfig;
    private static final String APPLICATION_NAME = "test-app";

    public static Stream<? extends Arguments> provideArguments() {
        return Stream.of(
                new TestCase()
                        .withTransactionId("tid-123")
                        .withEndpoints("localhost:1234", "main/base/path", "http://server2.com", "cars")
                        .withTransactionRequestHeader("banana", "yellow")
                        .withTransactionRequestHeader("Content-Type", "application/json")
                        .withTransactionResponseHeader("fruit", "banana")
                        .withClientRequestHeader("Authorization", "orange")
                        .withClientRequestHeader("Content-Type", "application/json")
                        .withClientRequestHeader("Pear", "green")
                        .withClientResponseHeader("Apple", "red")
                        .withHttpMethod(HttpMethod.POST, HttpMethod.PUT)
                        .withRequestPayload(carsPayload(), "SAAB")
                        .withResponsePayload(null, "OK")
        ).map(Arguments::of);
    }

    private static String carsPayload() {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("Cars", Arrays.asList("SAAB", "KIA", "Honda", "Fiat", "Porche", "Ford"));
            return TeliaObjectMapper.get().writeValueAsString(map);
        } catch(JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    public void setUp() {
        loggingConfig = new LoggingConfig();
        loggingConfig.init();
    }

    @ParameterizedTest
    @MethodSource("provideArguments")
    public void testLogTransactionRequest(TestCase testCase) {
        DefaultRequestLogger requestLogger = new DefaultRequestLogger(loggingConfig, APPLICATION_NAME);

        TransactionContext transactionContext = testCase.createTransactionContext();

        Optional<Map<String, Object>> oTransactionRequestMessage = requestLogger.getRequestMessage(transactionContext, testCase.transactionRequestPayload.getBytes(StandardCharsets.UTF_8), new RequestLoggingOptions(PayloadLoggingOption.TRUE, Level.INFO));

        Assertions.assertTrue(oTransactionRequestMessage.isPresent());
        Map<String, Object> message = oTransactionRequestMessage.get();
        Assertions.assertEquals("0", message.get(Constants.REQUEST_ID));
        Assertions.assertEquals(Constants.INBOUND, message.get(Constants.DIRECTION));
        Assertions.assertEquals(Constants.REQUEST, message.get(Constants.TYPE));
        Assertions.assertEquals(testCase.transactionHost, message.get(Constants.HOST));
        Assertions.assertEquals(testCase.transactionPath, message.get(Constants.ADDRESS));
        Assertions.assertEquals(testCase.transactionHttpMethod.name(), message.get(Constants.HTTP_METHOD));
        Assertions.assertEquals(testCase.transactionRequestHeaders.get("Content-Type").get(0), message.get(Constants.CONTENT_TYPE).toString());
        Assertions.assertEquals(testCase.transactionRequestHeaders.entrySet().toString(), base64DecodeHeaders(message));
        Assertions.assertEquals(testCase.transactionRequestPayload, base64DecodePayload(message));
    }

    @ParameterizedTest
    @MethodSource("provideArguments")
    public void testLogClientRequest(TestCase testCase) {
        DefaultRequestLogger requestLogger = new DefaultRequestLogger(loggingConfig, APPLICATION_NAME);

        TransactionContext transactionContext = testCase.createTransactionContext();
        RequestContext requestContext = testCase.createRequestContext(transactionContext);

        Optional<Map<String, Object>> oRequestMessage = requestLogger.getRequestMessage(requestContext, testCase.clientRequestPayload.getBytes(StandardCharsets.UTF_8), new RequestLoggingOptions(PayloadLoggingOption.TRUE, Level.INFO));

        Assertions.assertTrue(oRequestMessage.isPresent());
        Map<String, Object> message = oRequestMessage.get();
        Assertions.assertEquals("1", message.get(Constants.REQUEST_ID)); //Transaction is "0"
        Assertions.assertEquals(Constants.OUTBOUND, message.get(Constants.DIRECTION));
        Assertions.assertEquals(Constants.REQUEST, message.get(Constants.TYPE));
        Assertions.assertEquals(testCase.clientHost, message.get(Constants.HOST));
        Assertions.assertEquals(testCase.clientPath, message.get(Constants.ADDRESS));
        Assertions.assertEquals(testCase.clientHttpMethod.name(), message.get(Constants.HTTP_METHOD));
        Assertions.assertEquals(testCase.clientRequestHeaders.get("Content-Type").get(0), message.get(Constants.CONTENT_TYPE).toString());
        final String actualRequestHeaders = base64DecodeHeaders(message);
        Assertions.assertTrue(actualRequestHeaders.contains("Pear"));
        Assertions.assertTrue(actualRequestHeaders.contains("Authorization=[1x ************]"));
        Assertions.assertEquals(testCase.clientRequestPayload, base64DecodePayload(message));
    }

    @ParameterizedTest
    @MethodSource("provideArguments")
    public void testLogTransactionResponse(TestCase testCase) {
        DefaultRequestLogger requestLogger = new DefaultRequestLogger(loggingConfig, APPLICATION_NAME);

        TransactionContext transactionContext = testCase.createTransactionContext();

        Optional<Map<String, Object>> oTransactionResponseMessage = requestLogger.getResponseMessage(transactionContext, testCase.transactionResponsePayload, HttpStatus.OK, testCase.transactionResponseHeaders, new RequestLoggingOptions(PayloadLoggingOption.TRUE, Level.INFO));

        Assertions.assertTrue(oTransactionResponseMessage.isPresent());
        Map<String, Object> message = oTransactionResponseMessage.get();
        Assertions.assertEquals("0", message.get(Constants.REQUEST_ID));
        Assertions.assertEquals(Constants.OUTBOUND, message.get(Constants.DIRECTION));
        Assertions.assertEquals(Constants.RESPONSE, message.get(Constants.TYPE));
        Assertions.assertEquals(HttpStatus.OK.value(), message.get(Constants.RESPONSE_CODE));
        Assertions.assertEquals(testCase.transactionResponseHeaders.entrySet().toString(), base64DecodeHeaders(message));
        Assertions.assertEquals(testCase.transactionResponsePayload, message.get(Constants.PAYLOAD));
        Assertions.assertNotNull(message.get(Constants.REQUEST_DURATION));
    }

    @ParameterizedTest
    @MethodSource("provideArguments")
    public void testLogClientResponse(TestCase testCase) {
        DefaultRequestLogger requestLogger = new DefaultRequestLogger(loggingConfig, APPLICATION_NAME);

        TransactionContext transactionContext = testCase.createTransactionContext();
        RequestContext requestContext = testCase.createRequestContext(transactionContext);

        Optional<Map<String, Object>> oResponseMessage = requestLogger.getResponseMessage(requestContext, testCase.clientResponsePayload, HttpStatus.OK, testCase.clientResponseHeaders, new RequestLoggingOptions(PayloadLoggingOption.TRUE, Level.INFO));

        Assertions.assertTrue(oResponseMessage.isPresent());
        Map<String, Object> message = oResponseMessage.get();
        Assertions.assertEquals("1", message.get(Constants.REQUEST_ID));
        Assertions.assertEquals(Constants.INBOUND, message.get(Constants.DIRECTION));
        Assertions.assertEquals(Constants.RESPONSE, message.get(Constants.TYPE));
        Assertions.assertEquals(HttpStatus.OK.value(), message.get(Constants.RESPONSE_CODE));
        Assertions.assertEquals(testCase.clientResponseHeaders.entrySet().toString(), base64DecodeHeaders(message));
        Assertions.assertEquals(testCase.clientResponsePayload, base64DecodePayload(message));
        Assertions.assertNotNull(message.get(Constants.REQUEST_DURATION));
    }

    private String base64DecodeHeaders(Map<String, Object> message) {
        return new String(Base64.getDecoder().decode((String) message.get(Constants.HEADERS)), StandardCharsets.UTF_8);
    }

    private String base64DecodePayload(Map<String, Object> message) {
        return new String(Base64.getDecoder().decode((String) message.get(Constants.PAYLOAD)), StandardCharsets.UTF_8);
    }


    private static class TestCase {
        private HttpHeaders transactionRequestHeaders = new HttpHeaders();
        private HttpHeaders transactionResponseHeaders = new HttpHeaders();
        private HttpHeaders clientRequestHeaders = new HttpHeaders();
        private HttpHeaders clientResponseHeaders = new HttpHeaders();
        private HttpMethod transactionHttpMethod;
        private HttpMethod clientHttpMethod;
        private Map<String, List<String>> cookies = new HashMap<>();
        private Map<String, List<String>> uriVariables = new HashMap<>();
        private String transactionRequestPayload;
        private String clientRequestPayload;
        private String transactionResponsePayload;
        private String clientResponsePayload;
        private String transactionHost;
        private String transactionPath;
        private String clientHost;
        private String clientPath;
        private String transactionId;

        public TestCase withTransactionRequestHeader(String key, String value) {
            transactionRequestHeaders.put(key, Collections.singletonList(value));
            return this;
        }

        public TestCase withTransactionResponseHeader(String key, String value) {
            transactionResponseHeaders.put(key, Collections.singletonList(value));
            return this;
        }

        public TestCase withClientRequestHeader(String key, String value) {
            clientRequestHeaders.put(key, Collections.singletonList(value));
            return this;
        }

        public TestCase withClientResponseHeader(String key, String value) {
            clientResponseHeaders.put(key, Collections.singletonList(value));
            return this;
        }

        public TestCase withHttpMethod(HttpMethod transactionHttpMethod, HttpMethod clientHttpMethod) {
            this.transactionHttpMethod = transactionHttpMethod;
            this.clientHttpMethod = clientHttpMethod;
            return this;
        }

        public TestCase withRequestPayload(String transactionRequestPayload, String clientRequestPayload) {
            this.transactionRequestPayload = transactionRequestPayload;
            this.clientRequestPayload = clientRequestPayload;
            return this;
        }

        public TestCase withResponsePayload(String transactionResponsePayload, String clientResponsePayload) {
            this.transactionResponsePayload = transactionResponsePayload;
            this.clientResponsePayload = clientResponsePayload;
            return this;
        }

        public TestCase withEndpoints(String transactionHost, String transactionBasePath, String clientHost, String clientBasePath) {
            this.transactionHost = transactionHost;
            this.transactionPath = transactionBasePath;
            this.clientHost = clientHost;
            this.clientPath = clientBasePath;
            return this;
        }

        public TestCase withTransactionId(String transactionId) {
            this.transactionId = transactionId;
            return this;
        }

        public TransactionContext createTransactionContext() {
            return new RequestContextBuilder()
                    .withTid(transactionId)
                    .withUri(this.transactionHost, this.transactionPath)
                    .withQueryParams(this.uriVariables)
                    .withCookies(this.cookies)
                    .withHeaders(this.transactionRequestHeaders)
                    .withHttpMethod(this.transactionHttpMethod)
                    .buildTransactionContext();
        }

        public RequestContext createRequestContext(TransactionContext transactionContext) {
            return new RequestContextBuilder()
                    .withUri(this.clientHost, this.clientPath)
                    .withQueryParams(this.uriVariables)
                    .withCookies(this.cookies)
                    .withHeaders(this.clientRequestHeaders)
                    .withHttpMethod(this.clientHttpMethod)
                    .withTransactionContext(transactionContext)
                    .buildRequestContext();
        }
    }
}
