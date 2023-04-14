package com.teliacompany.webflux.request.processor;

import com.teliacompany.webflux.request.TransactionMetaDataProducer;
import com.teliacompany.webflux.request.log.RequestLoggingOptions;
import com.teliacompany.webflux.request.processor.model.InternalServerHttpRequest;
import com.teliacompany.webflux.request.processor.model.ProcessInternalRequestData;
import com.teliacompany.webflux.request.processor.model.RequestBody;
import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Mono;

public interface InternalRequestProcessor {
    default InternalRequestProcessorCaller processInternal(ServerHttpRequest serverHttpRequest) {
        return new InternalRequestProcessorCaller(this, serverHttpRequest);
    }

    default InternalRequestProcessorCaller processInternal(ProcessInternalRequestData requestData) {
        return new InternalRequestProcessorCaller(this, new InternalServerHttpRequest(requestData));
    }

    default InternalRequestProcessorCaller processInternal() {
        return new InternalRequestProcessorCaller(this, new InternalServerHttpRequest());
    }


    <I, J, IC, O, OC> Mono<OC> processInternal(ServerHttpRequest serverHttpRequest,
                                               RequestBody<I, J> requestBody,
                                               RequestHandler<I, J, IC, O, OC> requestHandler,
                                               TransactionMetaDataProducer<I> loggingMetaDataProducer,
                                               RequestLoggingOptions requestLoggingOptions);

}
