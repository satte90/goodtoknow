package com.teliacompany.webflux.request.processor;

import com.teliacompany.webflux.request.TransactionMetaDataProducer;
import com.teliacompany.webflux.request.log.RequestLoggingOptions;
import com.teliacompany.webflux.request.processor.model.RequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public final class RestRequestProcessorCaller {
    private final RestRequestProcessor restRequestProcessor;
    private final ServerHttpRequest serverHttpRequest;
    private RequestLoggingOptions requestLoggingOptions = RequestLoggingOptions.defaults();

    RestRequestProcessorCaller(RestRequestProcessor restRequestProcessor, ServerHttpRequest serverHttpRequest) {
        this.restRequestProcessor = restRequestProcessor;
        this.serverHttpRequest = serverHttpRequest;
    }

    /**
     * Usable when you do have a request body and no other variables
     * You can the optionally inject an inputConverter using withRequestBodyConverter
     */
    public <I> WithRequest<I, Void, I> withRequestBody(Class<I> requestBodyClass) {
        return new WithRequest<>(requestBodyClass, null, i -> i);
    }

    /**
     * Usable when you dont have a request body (! POST, PUT...) but have a path variable or request parameter
     * You can the optionally inject an inputConverter using withRequestConverter, withRequestObjectConverter
     */
    public <I> WithRequest<I, Void, I> withRequestObject(I requestObject) {
        return new WithRequest<>(null, requestObject, i -> i);
    }

    /**
     * Usable when you have both a requestBody and a pathVariable or requestParameter
     * You can the optionally inject an inputConverter using withRequestConverter, withRequestBodyConverter or withRequestObjectConverter
     */
    public <I, J> WithBiRequest<I, J, I> withRequestBodyAndObject(Class<I> requestBodyClass, J requestObject) {
        return new WithBiRequest<>(requestBodyClass, null, requestObject, (i, J) -> i);
    }

    /**
     * Usable when you dont have a request body (! POST, PUT...) but have a path variable or request parameter
     * You can the optionally inject an inputConverter using withRequestConverter, withRequestObjectConverter
     */
    public <I, J> WithBiRequest<I, J, I> withRequestObjects(I requestObject1, J requestObject2) {
        return new WithBiRequest<>(null, requestObject1, requestObject2, (i, j) -> i);
    }

    /**
     * Use this when you don't have any input for your service method or want to bypass automatic input handling and converting.
     * The "bypass" process:
     * requestProcessor.process(request)
     *    .withoutRequestBody()
     *    .withHandler(() -> service.callMyMethod(someParam, otherParam, extraParam))
     */
    public WithoutRequest withoutRequestBody() {
        return new WithoutRequest();
    }

    /**
     * For those cases where you just want to call a service method without input and don't need any special logging options nor meta data
     */
    public <O> Mono<ResponseEntity<Object>> withHandler(Supplier<Mono<O>> supplier) {
        return withoutRequestBody().withHandler(supplier);
    }


    /**
     * Second step in builder, can set input
     */
    public class WithBiRequest<I, J, IC> {
        private final RequestBody<I, J> requestBody;
        private final BiFunction<I, J, IC> inputConverter;
        private TransactionMetaDataProducer<I> transactionMetaDataProducer;

        private WithBiRequest(Class<I> requestBodyClass, I requestObject1, J requestObject2, BiFunction<I, J, IC> inputConverter) {
            this.requestBody = new RequestBody<>(requestBodyClass, requestObject1, requestObject2);
            this.transactionMetaDataProducer = (i, h) -> new HashMap<>();
            this.inputConverter = inputConverter;
        }

        private WithBiRequest(RequestBody<I, J> requestBody, TransactionMetaDataProducer<I> tMdp, BiFunction<I, J, IC> inputConverter) {
            this.requestBody = requestBody;
            this.transactionMetaDataProducer = tMdp;
            this.inputConverter = inputConverter;
        }

        /**
         * Sets the inputConverter, only one input converter can be used, will override any previous value
         * Use this if you which to convert requestBody or requestObject1 and requestObject2 to a new object that will be sent to handler function
         */
        public <IC2> WithRequest<I, J, IC2> withRequestConverter(BiFunction<I, J, IC2> inputConverter) {
            return new WithRequest<>(requestBody, transactionMetaDataProducer, inputConverter);
        }

        /**
         * Sets the inputConverter, only one input converter can be used, will override any previous value
         * Use this if you which to convert requestBody or requestObject1 only. Converted object and RequestObject2 will be sent to handler function
         */
        public <IC2> WithBiRequest<I, J, IC2> withRequestConverter(Function<I, IC2> inputConverter) {
            return new WithBiRequest<>(requestBody, transactionMetaDataProducer, (i, j) -> inputConverter.apply(i));
        }

        /**
         * Set extra meta data producer, only one can be used, will override any previous value
         */
        public WithBiRequest<I, J, IC> withMetaData(TransactionMetaDataProducer<I> metaDataProducer) {
            transactionMetaDataProducer = metaDataProducer;
            return this;
        }

        /**
         * Set special logging options for the request, only one can be used, will override any previous value
         */
        public WithBiRequest<I, J, IC> withLoggingOptions(RequestLoggingOptions loggingOptions) {
            requestLoggingOptions = loggingOptions;
            return this;
        }

        /**
         * Set output converter, output converter is applied after handle function.
         * Defaults to o -> o
         */
        public <O, OC> WithBiRequestAndOutputConverter<O, OC> withOutputConverter(Function<O, OC> outputConverter) {
            return new WithBiRequestAndOutputConverter<>(outputConverter);
        }

        /**
         * Set handler function, build and execute! Use this when you expect two input arguments
         */
        public <O> Mono<ResponseEntity<Object>> withHandler(BiFunction<IC, J, Mono<O>> handler) {
            RequestHandler<I, J, IC, O, O> rh = new RequestHandler<>(handler, inputConverter, o -> o);
            return restRequestProcessor.processRequest(serverHttpRequest, requestBody, rh, transactionMetaDataProducer, requestLoggingOptions);
        }

        public class WithBiRequestAndOutputConverter<O, OC> {
            private final Function<O, OC> outputConverter;

            private WithBiRequestAndOutputConverter(Function<O, OC> outputConverter) {
                this.outputConverter = outputConverter;
            }

            /**
             * Set handler function, build and execute! Use this when you expect two input arguments
             */
            public Mono<ResponseEntity<Object>> withHandler(BiFunction<IC, J, Mono<O>> handler) {
                RequestHandler<I, J, IC, O, OC> rh = new RequestHandler<>(handler, inputConverter, outputConverter);
                return restRequestProcessor.processRequest(serverHttpRequest, requestBody, rh, transactionMetaDataProducer, requestLoggingOptions);
            }
        }
    }

    /**
     * Second step in builder, can set input
     * J can only be used for input converter using the "copy" constructor and request coming from WithBiRequest.
     * This happens when user have 2 inputs and wants to convert them to 1 input basically. Otherwise J is always Void
     */
    public class WithRequest<I, J, IC> {
        private final RequestBody<I, J> requestBody;
        private final BiFunction<I, J, IC> inputConverter;
        private TransactionMetaDataProducer<I> transactionMetaDataProducer;

        private WithRequest(Class<I> requestBodyClass, I requestObject1, Function<I, IC> inputConverter) {
            this.requestBody = new RequestBody<>(requestBodyClass, requestObject1, null);
            this.transactionMetaDataProducer = (i, h) -> new HashMap<>();
            this.inputConverter = (i, v) -> inputConverter.apply(i);
        }

        private WithRequest(RequestBody<I, J> requestBody, TransactionMetaDataProducer<I> tMdp, BiFunction<I, J, IC> inputConverter) {
            this.requestBody = requestBody;
            this.transactionMetaDataProducer = tMdp;
            this.inputConverter = inputConverter;
        }

        /**
         * Sets the inputConverter, only one input converter can be used, will override any previous value
         */
        public <IC2> WithRequest<I, J, IC2> withRequestConverter(Function<I, IC2> inputConverter) {
            return new WithRequest<>(requestBody, transactionMetaDataProducer, (i, j) -> inputConverter.apply(i));
        }

        /**
         * Set extra meta data producer, only one can be used, will override any previous value
         */
        public WithRequest<I, J, IC> withMetaData(TransactionMetaDataProducer<I> metaDataProducer) {
            transactionMetaDataProducer = metaDataProducer;
            return this;
        }

        /**
         * Set special logging options for the request, only one can be used, will override any previous value
         */
        public WithRequest<I, J, IC> withLoggingOptions(RequestLoggingOptions loggingOptions) {
            requestLoggingOptions = loggingOptions;
            return this;
        }

        /**
         * Set output converter, output converter is applied after handle function.
         * Defaults to o -> o
         */
        public <O, OC> WithRequestAndOutputConverter<O, OC> withOutputConverter(Function<O, OC> outputConverter) {
            return new WithRequestAndOutputConverter<>(outputConverter);
        }

        /**
         * Set handler function, build and execute! Use this when you expect one input argument
         */
        public <O> Mono<ResponseEntity<Object>> withHandler(Function<IC, Mono<O>> handler) {
            RequestHandler<I, J, IC, O, O> rh = new RequestHandler<>(handler, inputConverter, o -> o);
            return restRequestProcessor.processRequest(serverHttpRequest, requestBody, rh, transactionMetaDataProducer, requestLoggingOptions);
        }

        public class WithRequestAndOutputConverter<O, OC> {
            private final Function<O, OC> outputConverter;

            private WithRequestAndOutputConverter(Function<O, OC> outputConverter) {
                this.outputConverter = outputConverter;
            }

            /**
             * Set handler function, build and execute! Use this when you expect one input argument
             */
            public Mono<ResponseEntity<Object>> withHandler(Function<IC, Mono<O>> handler) {
                RequestHandler<I, J, IC, O, OC> rh = new RequestHandler<>(handler, inputConverter, outputConverter);
                return restRequestProcessor.processRequest(serverHttpRequest, requestBody, rh, transactionMetaDataProducer, requestLoggingOptions);
            }
        }
    }

    /**
     * Second step in builder, can set input
     */
    public class WithoutRequest {
        private final RequestBody<Void, Void> requestBody;
        private TransactionMetaDataProducer<Void> transactionMetaDataProducer;

        private WithoutRequest() {
            this.requestBody = new RequestBody<>(Void.class, null, null);
            this.transactionMetaDataProducer = (i, h) -> new HashMap<>();
        }

        /**
         * Set extra meta data producer, only one can be used, will override any previous value
         */
        public WithoutRequest withMetaData(TransactionMetaDataProducer<Void> metaDataProducer) {
            transactionMetaDataProducer = metaDataProducer;
            return this;
        }

        /**
         * Set special logging options for the request, only one can be used, will override any previous value
         */
        public WithoutRequest withLoggingOptions(RequestLoggingOptions loggingOptions) {
            requestLoggingOptions = loggingOptions;
            return this;
        }

        /**
         * Set output converter, output converter is applied after handle function.
         * Defaults to o -> o
         */
        public <O, OC> WithoutRequestAndOutputConverter<O, OC> withOutputConverter(Function<O, OC> outputConverter) {
            return new WithoutRequestAndOutputConverter<>(outputConverter);
        }

        /**
         * Set handler function, build and execute! Use this when you don't need any input for the handler function
         */
        public <O> Mono<ResponseEntity<Object>> withHandler(Supplier<Mono<O>> supplier) {
            RequestHandler<Void, Void, Void, O, O> rh = new RequestHandler<>(supplier, o -> o);
            return restRequestProcessor.processRequest(serverHttpRequest, requestBody, rh, transactionMetaDataProducer, requestLoggingOptions);
        }

        public class WithoutRequestAndOutputConverter<O, OC> {
            private final Function<O, OC> outputConverter;

            private WithoutRequestAndOutputConverter(Function<O, OC> outputConverter) {
                this.outputConverter = outputConverter;
            }

            /**
             * Set handler function, build and execute! Use this when you don't need any input for the handler function
             */
            public Mono<ResponseEntity<Object>> withHandler(Supplier<Mono<O>> supplier) {
                RequestHandler<Void, Void, Void, O, OC> rh = new RequestHandler<>(supplier, outputConverter);
                return restRequestProcessor.processRequest(serverHttpRequest, requestBody, rh, transactionMetaDataProducer, requestLoggingOptions);
            }
        }
    }
}
