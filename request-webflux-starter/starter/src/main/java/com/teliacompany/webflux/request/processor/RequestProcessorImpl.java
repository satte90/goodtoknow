package com.teliacompany.webflux.request.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teliacompany.webflux.error.api.ErrorCause;
import com.teliacompany.webflux.error.exception.WebException;
import com.teliacompany.webflux.error.exception.client.BadRequestException;
import com.teliacompany.webflux.error.exception.server.InternalServerErrorException;
import com.teliacompany.webflux.jackson.TeliaObjectMapper;
import com.teliacompany.webflux.request.RequestProcessor;
import com.teliacompany.webflux.request.TransactionMetaDataProducer;
import com.teliacompany.webflux.request.context.RequestContextBuilder;
import com.teliacompany.webflux.request.context.TransactionContext;
import com.teliacompany.webflux.request.context.TransactionResponse;
import com.teliacompany.webflux.request.log.RequestLogger;
import com.teliacompany.webflux.request.log.RequestLoggingOptions;
import com.teliacompany.webflux.request.log.trace.TraceLogger;
import com.teliacompany.webflux.request.metrics.MetricsReporter;
import com.teliacompany.webflux.request.processor.error.ErrorAttributesProvider;
import com.teliacompany.webflux.request.processor.error.ReadOnlyWebException;
import com.teliacompany.webflux.request.processor.error.TraceLoggerProvider;
import com.teliacompany.webflux.request.processor.model.RequestBody;
import com.teliacompany.webflux.request.utils.ByteStreamUtil;
import com.teliacompany.webflux.request.utils.Constants;
import com.teliacompany.webflux.request.utils.TransactionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.slf4j.MDC;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.singletonList;

@SuppressWarnings("unchecked")
public final class RequestProcessorImpl implements RequestProcessor {
    private static final Logger LOG = LogManager.getLogger(RequestProcessorImpl.class);
    private static final ObjectMapper OBJECT_MAPPER = TeliaObjectMapper.get();

    private final RequestLogger requestLogger;
    private final MetricsReporter metricsReporter;
    private final List<ErrorAttributesProvider> errorAttributesProviders;
    private final TraceLoggerProvider traceLogProvider;

    public RequestProcessorImpl(RequestLogger requestLogger, MetricsReporter metricsReporter, List<ErrorAttributesProvider> errorAttributesProviders) {
        this.requestLogger = requestLogger;
        this.metricsReporter = metricsReporter;
        this.errorAttributesProviders = errorAttributesProviders;
        this.traceLogProvider = errorAttributesProviders.stream()
                .filter(errorAttributesProvider -> errorAttributesProvider instanceof TraceLoggerProvider)
                .map(errorAttributesProvider -> (TraceLoggerProvider) errorAttributesProvider)
                .findFirst()
                .orElseThrow(() -> new InternalServerErrorException("Could not find required bean TraceLoggerProvider"));
    }

    /**
     * The "Chain Definition" used when processing requests from a rest controller (classic way but still works in webflux)
     * Keep this clean, put logic in other methods, pass along the Context class.
     */
    @Override
    public <I, J, IC, O, OC> Mono<ResponseEntity<Object>> processRequest(ServerHttpRequest serverHttpRequest,
                                                                         RequestBody<I, J> requestBody,
                                                                         RequestHandler<I, J, IC, O, OC> requestHandler,
                                                                         TransactionMetaDataProducer<I> loggingMetaDataProducer,
                                                                         RequestLoggingOptions requestLoggingOptions) {
        // Note: The context is set up first, before chain is executed (subscriberContext(ctx -> {...})
        final TransactionContext transactionContext = new RequestContextBuilder()
                .withHeaders(serverHttpRequest.getHeaders())
                .withCookies(serverHttpRequest.getCookies())
                .withHttpMethod(serverHttpRequest.getMethod())
                .withQueryParams(serverHttpRequest.getQueryParams())
                .withUri(serverHttpRequest.getURI())
                .buildTransactionContext();

        // Initiates a new LocalContext with initial data from process setup definition.
        LocalContext<I, J, IC, O, OC> localContext = new LocalContext<>(requestBody.getRequestBodyClass(), requestHandler, loggingMetaDataProducer, requestLoggingOptions);

        return startRequest(transactionContext, localContext)
                .flatMap(context -> this.setRequestBody(serverHttpRequest, requestBody, context))
                .map(this::deserializeRequestBody)
                .flatMap(this::addTransactionMetaData)
                .flatMap(this::logRequest)
                .map(this::runInputConverter)
                .map(this::clearRequestBody)
                .flatMap(this::runMainFunction)
                .map(this::runOutputConverter)
                .onErrorResume(e -> abortTransactionWithError(e, localContext))
                .flatMap(this::createTransactionResponse)
                .flatMap(this::logResponse)
                .map(this::returnResponseEntity)
                .contextWrite(ctx -> ctx
                        .put(Constants.TRANSACTION_CONTEXT_KEY, transactionContext)
                        .put(TraceLogger.class, TraceLogger.create(serverHttpRequest.getHeaders().getFirst(Constants.HTTP_X_TRACING)))
                );
    }

    /**
     * The "Chain Definition" used when processing requests for an internal process, such as a scheduled execution or a filter.
     */
    @Override
    public <I, J, IC, O, OC> Mono<OC> processInternal(ServerHttpRequest serverHttpRequest, RequestBody<I, J> requestBody, RequestHandler<I, J, IC, O, OC> requestHandler, TransactionMetaDataProducer<I> loggingMetaDataProducer, RequestLoggingOptions requestLoggingOptions) {
        final TransactionContext transactionContext = new RequestContextBuilder()
                .withHeaders(serverHttpRequest.getHeaders())
                .withUri(serverHttpRequest.getURI())
                .buildTransactionContext();

        LocalContext<I, J, IC, O, OC> localContext = new LocalContext<>(requestBody.getRequestBodyClass(), requestHandler, loggingMetaDataProducer, requestLoggingOptions);
        return startRequest(transactionContext, localContext)
                .map(context -> {
                    context.requestObject1 = requestBody.getRequestObject1();
                    context.requestObject2 = requestBody.getRequestObject2();
                    return context;
                })
                .flatMap(this::addTransactionMetaData)
                .flatMap(this::logInternalRequest)
                .map(this::runInputConverter)
                .flatMap(this::runMainFunction)
                .map(this::runOutputConverter)
                .onErrorResume(e -> abortTransactionWithError(e, localContext))
                .flatMap(ctx -> Mono.justOrEmpty(ctx.convertedResponse))
                .contextWrite(ctx -> ctx.put(Constants.TRANSACTION_CONTEXT_KEY, transactionContext)
                        .put(TraceLogger.class, TraceLogger.create(serverHttpRequest.getHeaders().getFirst(Constants.HTTP_X_TRACING)))
                );
    }

    /***********************************************************************************************************/

    /**
     * Add TransactionId, Tcad and Tscid to MDC (Mapped Diagnostic Context)
     * > "Mapped Diagnostic Context provides a way to enrich log messages with information that
     * >  could be unavailable in the scope where the logging actually occurs but that can be indeed useful to better track the execution of the program."
     * > https://www.baeldung.com/mdc-in-log4j-2-logback
     */
    private <I, J, IC, O, OC> Mono<LocalContext<I, J, IC, O, OC>> startRequest(TransactionContext tCtx, LocalContext<I, J, IC, O, OC> localContext) {
        MDC.put(Constants.MDC_TRANSACTION_ID_KEY, tCtx.getTid());
        MDC.put(Constants.MDC_TCAD_KEY, tCtx.getTcad());
        MDC.put(Constants.MDC_TSCID_KEY, tCtx.getTscid());

        return Mono.just(localContext);
    }

    /**
     * Set the requestBody in localContext from either the http request payload or the manually set requestObject1.
     * <p>
     * If requestProcessor.process(...) has been set up using .withRequestBody(Class<I>) or .withRequestBodyAndObject(Class<I>, J) and Class<I> is not Void then consume
     * the http request payload as a byte array. This is done by reading the body databuffer stream and continuously releasing the data buffers to avoid memory leaks.
     * When the whole data stream is consumed store it in the LocalContext.rawRequestBody field.
     * <p>
     * If on the other hand no request body class has been defined, i.e .withoutRequestBody() or .withRequestObject(I) or .withRequestObjects(I, J) then simply store
     * requestObject1 (I) in LocalContext.requestObject1.
     * <p>
     * RequestObject2 is always stored in LocalContext.requestObject2, but it will be null in certain cases, such as when .withoutRequestBody() or .withRequestObject(I)
     * has been used.
     */
    private <I, J, IC, O, OC> Mono<LocalContext<I, J, IC, O, OC>> setRequestBody(ServerHttpRequest request, RequestBody<I, J> requestBody, LocalContext<I, J, IC, O, OC> localContext) {
        localContext.requestObject2 = requestBody.getRequestObject2();

        if(ServerHttpRequest.class.equals(localContext.requestBodyClass)) {
            //If handler function wants the actual server request, set it directly as requestObject
            localContext.requestObject1 = (I) request;
        } else if(localContext.requestBodyClass != null && !Void.class.equals(localContext.requestBodyClass)) {
            // Consume request body into a raw byte[] if an expected class is set and requestObject1 is NOT manually set
            return request.getBody()
                    .map(dataBuffer -> {
                        ByteBuffer byteBuffer = dataBuffer.asByteBuffer();
                        byte[] byteArray = new byte[byteBuffer.remaining()];
                        byteBuffer.get(byteArray);
                        DataBufferUtils.release(dataBuffer);
                        return byteArray;
                    })
                    .reduce(new ByteArrayOutputStream(), ByteStreamUtil::writeBytesToStream)
                    .map(ByteArrayOutputStream::toByteArray)
                    .map(localContext::returnWithRawRequestBody);
        } else {
            // If requestObject1 is manually set, then it will be used instead of trying to parse requestBody
            localContext.requestObject1 = requestBody.getRequestObject1();
        }
        return Mono.just(localContext);
    }

    /**
     * Deserialize requestBody if needed
     * <pre>
     * If a request body is used:
     *    If it is NOT String.class:
     *      Assumes it is in JSON format, XML or other formats not supported at the moment (2022-03-02), if it will be needed use Content-Type header to detect what
     *      format (Json/xml/?) should be used.
     *
     *      Deserialize the request body json using the standard Telia Jackson Object Mapper provided by jackson-webflux-starter. The payload is stored in local context
     *      variable requestObject1.
     *    else
     *      Store string request body in requestObject1.
     * If request body is NOT used:
     *    do nothing
     * </pre>
     */
    private <I, J, IC, O, OC> LocalContext<I, J, IC, O, OC> deserializeRequestBody(LocalContext<I, J, IC, O, OC> localContext) {
        if(String.class.equals(localContext.requestBodyClass) && localContext.requestObject1 == null) {
            return localContext.returnWithRequestObject1((I) new String(localContext.getRawRequestBody(), StandardCharsets.UTF_8));
        }

        // RawRequestBody is only set if a requestBodyClass was specified and requestObject1 was NOT manually set
        return Optional.ofNullable(localContext.getRawRequestBody())
                .map(body -> {
                    try {
                        I convertedRequestBody = OBJECT_MAPPER.readValue(body, localContext.requestBodyClass);
                        return localContext.returnWithRequestObject1(convertedRequestBody);
                    } catch(IOException e) {
                        throw new BadRequestException(ErrorCause.from("Internal", e), "Could not parse request as {}", localContext.requestBodyClass);
                    } finally {
                        // TODO: test this actually works as expected...
                        this.logRequest(localContext);
                    }
                })
                .orElse(localContext);
    }

    /**
     * Runs the metadata producer method specified in the process setup and merge the result from this function with metadata extracted from http headers
     */
    private <I, J, IC, O, OC> Mono<LocalContext<I, J, IC, O, OC>> addTransactionMetaData(LocalContext<I, J, IC, O, OC> localContext) {
        return RequestProcessor.getTransactionContext()
                .map(transactionContext -> {
                    final Map<String, String> metaData = Optional
                            .ofNullable(localContext.metaDataProducer.apply(localContext.requestObject1, transactionContext.getRequest().getHeaders()))
                            .orElse(new HashMap<>());
                    final List<String> incomingMetaData = transactionContext.getRequest().getHeaders(Constants.HTTP_TRANSACTION_META_DATA_HEADER);
                    TransactionUtils.addMetaDataFromHeaders(incomingMetaData, metaData);
                    transactionContext.setMetaData(metaData);
                    return localContext;
                });
    }

    /**
     * Logs the request using requestLogger, either DefaultRequestLogger or DisabledRequestLogger. The latter one is only used when logging has been disabled.
     */
    private <I, J, IC, O, OC> Mono<LocalContext<I, J, IC, O, OC>> logRequest(LocalContext<I, J, IC, O, OC> localContext) {
        return RequestProcessor.getTransactionContext()
                .map(transactionContext -> {
                    requestLogger.logRequest(transactionContext, localContext.getRawRequestBody(), localContext.requestLoggingOptions);
                    return localContext;
                })
                .flatMap(TraceLogger.logWithTransaction("Start transaction with tid: {}", transactionContext -> singletonList(transactionContext.getTid())));
    }

    /**
     * Logs the internal request using requestLogger, either DefaultRequestLogger or DisabledRequestLogger. The latter one is only used when logging has been disabled.
     */
    private <I, J, IC, O, OC> Mono<LocalContext<I, J, IC, O, OC>> logInternalRequest(LocalContext<I, J, IC, O, OC> localContext) {
        return RequestProcessor.getTransactionContext()
                .map(transactionContext -> {
                    requestLogger.logInternalRequest(transactionContext, localContext.requestLoggingOptions);
                    return localContext;
                })
                .flatMap(TraceLogger.logWithTransaction("Start internal transaction with tid: {}", transactionContext -> singletonList(transactionContext.getTid())));
    }

    /**
     * Runs the input converter specified in the process setup. Note that this is always executed and there will always be an input converter even if none was defined in
     * process setup, defaults to simple (I, J) -> IC
     */
    private <I, J, IC, O, OC> LocalContext<I, J, IC, O, OC> runInputConverter(LocalContext<I, J, IC, O, OC> localContext) {
        LOG.debug("Executing input converter");
        localContext.convertedRequestObject = localContext.requestHandler.convertInput(localContext.requestObject1, localContext.requestObject2);
        return localContext;
    }

    /**
     * Set request body (string) to null in the local context, no need to keep it in memory during the whole request chain...
     */
    private <I, J, IC, O, OC> LocalContext<I, J, IC, O, OC> clearRequestBody(LocalContext<I, J, IC, O, OC> localContext) {
        return localContext.returnWithRawRequestBody(null);
    }

    /**
     * Executes the main function defined in .withHandler(...) in process setup.
     */
    private <I, J, IC, O, OC> Mono<LocalContext<I, J, IC, O, OC>> runMainFunction(LocalContext<I, J, IC, O, OC> localContext) {
        return localContext.requestHandler.applyHandler(localContext.convertedRequestObject, localContext.requestObject2)
                .map(localContext::returnWithResponseAndClearRequest)
                .switchIfEmpty(Mono.defer(() -> {
                    LOG.debug("Main function returned empty mono. Returning request context with null response");
                    return Mono.just(localContext);
                }))
                .flatMap(TraceLogger.log("<RequestProcessor> Finished running main function"));
    }

    /**
     * Executes the outputConverter with input being the output of the mainFunction. An output converter will always be available, if none were specified in the process
     * setup then a simple o -> o converter will be used.
     */
    private <I, J, IC, O, OC> LocalContext<I, J, IC, O, OC> runOutputConverter(LocalContext<I, J, IC, O, OC> localContext) {
        LOG.debug("Executing output converter");
        localContext.convertedResponse = localContext.requestHandler.convertOutput(localContext.response);
        return localContext;
    }

    /**
     * Aborts the transaction on error.
     * - Record transaction duration
     * - Log Error response
     * - Report error metrics
     * - Wrap non WebExceptions in an InternalServerError
     * - Extend WebException with ErrorAttributes
     */
    private <I, J, IC, O, OC> Mono<LocalContext<I, J, IC, O, OC>> abortTransactionWithError(Throwable error, LocalContext<I, J, IC, O, OC> localContext) {
        return RequestProcessor.getContext()
                .flatMap(contextWrapper -> {
                    TransactionContext transactionContext = contextWrapper.getTransactionContext();
                    transactionContext.recordRequestDuration();

                    requestLogger.logError(transactionContext, error, localContext.requestLoggingOptions);
                    metricsReporter.reportRequestError(transactionContext, error);
                    final WebException webException;
                    if(error instanceof WebException) {
                        webException = (WebException) error;
                    } else {
                        webException = new InternalServerErrorException(ErrorCause.from("internal", error), "Error in transaction");
                    }
                    ReadOnlyWebException readOnlyWebException = new ReadOnlyWebException(webException);
                    errorAttributesProviders.forEach(provider -> webException.getExtraAttributes().putAll(provider.getErrorAttributes(readOnlyWebException, contextWrapper)));
                    return Mono.error(webException);
                });

    }

    /**
     * Creates the final transaction response including json serialization of response body if needed.
     * - Record transaction duration
     * - Create transaction response object with transaction context metadata such as tid, tscId, tcad etc
     * - Check if response is empty, if so return a transaction response with HttpStatus = NO_CONTENT
     * - Else continue with json serialization of response, if response is not already a String or other resource not possible to serialize
     * - Set HttpStatus to OK and return
     */
    private <I, J, IC, O, OC> Mono<LocalContext<I, J, IC, O, OC>> createTransactionResponse(LocalContext<I, J, IC, O, OC> localContext) {
        return RequestProcessor.getContext()
                .map(context -> {
                    TransactionContext transactionContext = context.getTransactionContext();
                    transactionContext.recordRequestDuration();

                    TransactionResponse response = new TransactionResponse()
                            .setHttpHeader(Constants.HTTP_TRANSACTION_ID_HEADER, transactionContext.getTid())
                            .setHttpHeader(Constants.HTTP_REQUEST_ID_HEADER, transactionContext.getTid())
                            .setHttpHeader(Constants.HTTP_CORRELATION_ID_HEADER, transactionContext.getTid())
                            .setHttpHeader(Constants.HTTP_TRANSACTION_META_DATA_HEADER, TransactionUtils.getMetaDataAsHeaderValue(transactionContext))
                            .setHttpHeader(Constants.HTTP_X_TCAD, transactionContext.getTcad())
                            .setHttpHeader(Constants.HTTP_X_TSCID, transactionContext.getTscid());

                    if(localContext.convertedResponse == null) {
                        return response.setHttpStatus(HttpStatus.NO_CONTENT);
                    }

                    Object responseBody = localContext.convertedResponse;
                    // Try to json serialize response body if it is not already a string or an InputStreamResource
                    if(shouldSerializeToJson(responseBody)) {
                        try {
                            responseBody = OBJECT_MAPPER.writeValueAsString(responseBody);
                        } catch(JsonProcessingException e) {
                            LOG.error("Could not serialize response body to json!", e);
                        }
                    }

                    if(transactionContext.isTracingEnabled()) {
                        final String traceLogJsonString = traceLogProvider.getTraceLogJson(context.getTraceLogger().getLog());
                        response.setHttpHeader(Constants.HTTP_X_TRACE_LOG, traceLogJsonString);
                    }

                    return response
                            .setHttpStatus(HttpStatus.OK)
                            .setBody(responseBody);
                })
                .map(localContext::returnWithTransactionResponse);
    }

    private boolean shouldSerializeToJson(Object object) {
        return !(object instanceof String) && !(object instanceof InputStreamResource);
    }

    /**
     * Logs the successful response (Errors will not reach this method)
     * Make sure MDC contains the metadata needed, this will be picked up by log4j, see log4j config files
     * Log response using request logger and metrics using metricsReporter
     */
    private <I, J, IC, O, OC> Mono<LocalContext<I, J, IC, O, OC>> logResponse(LocalContext<I, J, IC, O, OC> localContext) {
        return RequestProcessor.getTransactionContext()
                .map(transactionContext -> {
                    // Make sure these are in MDC when response is being logged! These can be picked up by log4j automatically
                    MDC.put(Constants.MDC_TRANSACTION_ID_KEY, transactionContext.getTid());
                    MDC.put(Constants.MDC_TCAD_KEY, transactionContext.getTcad());
                    MDC.put(Constants.MDC_TSCID_KEY, transactionContext.getTscid());

                    TransactionResponse response = localContext.transactionResponse;
                    requestLogger.logResponse(transactionContext, response, localContext.requestLoggingOptions);
                    metricsReporter.reportRequestData(transactionContext, response.getHttpStatus());
                    return localContext;
                });
    }

    /**
     * Creates a ResponseEntity with data from the transactionResponse object.
     */
    private <I, J, IC, O, OC> ResponseEntity<Object> returnResponseEntity(LocalContext<I, J, IC, O, OC> localContext) {
        return ResponseEntity
                .status(localContext.transactionResponse.getHttpStatus())
                .headers(localContext.transactionResponse.getHttpHeaders())
                .body(localContext.transactionResponse.getBody());
    }

    private static final class LocalContext<I, J, IC, O, OC> {
        private final Class<I> requestBodyClass;
        private final RequestHandler<I, J, IC, O, OC> requestHandler;
        private final TransactionMetaDataProducer<I> metaDataProducer;
        private final RequestLoggingOptions requestLoggingOptions;

        private byte[] rawRequestBody;
        private I requestObject1; //Either deserialized requestBody or manually set
        private J requestObject2;
        private IC convertedRequestObject;
        private O response;
        private OC convertedResponse;
        private TransactionResponse transactionResponse;

        LocalContext(Class<I> requestBodyClass, RequestHandler<I, J, IC, O, OC> requestHandler, TransactionMetaDataProducer<I> metaDataProducer, RequestLoggingOptions requestLoggingOptions) {
            this.requestBodyClass = requestBodyClass;
            this.requestHandler = requestHandler;
            this.metaDataProducer = metaDataProducer;
            this.requestLoggingOptions = requestLoggingOptions;
        }

        byte[] getRawRequestBody() {
            return rawRequestBody;
        }

        LocalContext<I, J, IC, O, OC> returnWithRawRequestBody(byte[] rawRequestBody) {
            this.rawRequestBody = rawRequestBody;
            return this;
        }

        LocalContext<I, J, IC, O, OC> returnWithRequestObject1(I convertedRequestBody) {
            this.requestObject1 = convertedRequestBody;
            return this;
        }

        LocalContext<I, J, IC, O, OC> returnWithResponseAndClearRequest(O response) {
            this.requestObject1 = null;
            this.requestObject2 = null;
            this.response = response;
            return this;
        }

        LocalContext<I, J, IC, O, OC> returnWithTransactionResponse(TransactionResponse transactionResponse) {
            this.transactionResponse = transactionResponse;
            return this;
        }
    }
}
