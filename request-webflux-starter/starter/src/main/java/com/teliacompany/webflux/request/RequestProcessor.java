package com.teliacompany.webflux.request;

import com.teliacompany.webflux.request.context.ContextWrapper;
import com.teliacompany.webflux.request.context.RequestContext;
import com.teliacompany.webflux.request.context.TransactionContext;
import com.teliacompany.webflux.request.log.trace.InactiveTraceLogger;
import com.teliacompany.webflux.request.log.trace.TraceLogger;
import com.teliacompany.webflux.request.processor.InternalRequestProcessor;
import com.teliacompany.webflux.request.processor.RestRequestProcessor;
import com.teliacompany.webflux.request.utils.Constants;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.function.Supplier;

/**
 * Generally you should autowire the specific request processor for your use-case instead.
 * Choose between:
 * - RestRequestProcessor
 * - InternalRequestProcessor
 * - RouterRequestProcessor
 * <p>
 * Use this for static access of transaction and request context as well as schedule blocking calls
 */
public interface RequestProcessor extends RestRequestProcessor, InternalRequestProcessor {
    static Mono<TransactionContext> getTransactionContext() {
        return Mono.deferContextual(subscriberContext -> Mono.just(subscriberContext.get(Constants.TRANSACTION_CONTEXT_KEY)));
    }

    static Mono<RequestContext> getRequestContext() {
        return Mono.deferContextual(subscriberContext -> Mono.just(subscriberContext.get(Constants.REQUEST_CONTEXT_KEY)));
    }

    static Mono<TraceLogger> getTraceLogger() {
        return Mono.deferContextual(subscriberContext -> Mono.just(subscriberContext.get(TraceLogger.class)));
    }

    static Mono<ContextWrapper> getContext() {
        return Mono.deferContextual(subscriberContext -> {
            final TraceLogger traceLogger = subscriberContext.getOrDefault(TraceLogger.class, new InactiveTraceLogger());
            final RequestContext requestContext = subscriberContext.getOrDefault(Constants.REQUEST_CONTEXT_KEY, null);
            final TransactionContext transactionContext = subscriberContext.get(Constants.TRANSACTION_CONTEXT_KEY);
            return Mono.just(new ContextWrapper(transactionContext, requestContext, traceLogger));
        });
    }

    /**
     * Run blocking stuff on thread from boundedElastic thread pool.
     */
    static <R> Mono<R> scheduleBlocking(Supplier<R> blockingStuff) {
        return getTransactionContext()
                .publishOn(Schedulers.boundedElastic())
                .map(ctx -> blockingStuff.get());
    }
}
