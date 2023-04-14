package com.teliacompany.webflux.request.processor;

import com.teliacompany.webflux.request.TransactionMetaDataProducer;
import com.teliacompany.webflux.request.log.RequestLoggingOptions;
import com.teliacompany.webflux.request.processor.model.RequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Mono;

public interface RestRequestProcessor {

    default RestRequestProcessorCaller process(ServerHttpRequest serverHttpRequest) {
        return new RestRequestProcessorCaller(this, serverHttpRequest);
    }

    /**
     * Prefer using process(ServerHttpRequest serverHttpRequest).
     *
     * @param serverHttpRequest       - The request
     * @param requestBody             - Contains the expected request class and optionally a manually created request object
     * @param requestHandler          - Handler and converter functions
     * @param loggingMetaDataProducer - meta data producer for logging
     * @param requestLoggingOptions   - Request logging options
     * @param <I>                     - Input
     * @param <O>                     - Output
     * @return Mono<ResponseEntity < O>>
     */
    <I, J, IC, O, OC> Mono<ResponseEntity<Object>> processRequest(
            ServerHttpRequest serverHttpRequest,
            RequestBody<I, J> requestBody,
            RequestHandler<I, J, IC, O, OC> requestHandler,
            TransactionMetaDataProducer<I> loggingMetaDataProducer,
            RequestLoggingOptions requestLoggingOptions);
}
