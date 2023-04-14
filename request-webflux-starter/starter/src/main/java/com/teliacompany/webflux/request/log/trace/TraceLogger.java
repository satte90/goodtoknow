package com.teliacompany.webflux.request.log.trace;

import com.teliacompany.webflux.request.context.TransactionContext;
import com.teliacompany.webflux.request.RequestProcessor;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public interface TraceLogger {
    static TraceLogger create(boolean active) {
        return active ? new ActiveTraceLogger() : new InactiveTraceLogger();
    }

    static TraceLogger create(String headerValue) {
        return headerValue != null ? create(Boolean.parseBoolean(headerValue)) : new InactiveTraceLogger();
    }

    void addLogEntry(String messageTemplate, Object... objects);

    void addLogEntry(String messageTemplate, List<Object> ctxObjects, Object... objects);

    List<TraceLogEntry> getLog();

    static <T> Function<T, Mono<T>> log(String message, Object... object) {
        return type -> RequestProcessor.getTraceLogger().map(traceLogger -> {
            traceLogger.addLogEntry(message, object);
            return type;
        });
    }

    @SafeVarargs
    static <T> Function<T, Mono<T>> log(String message, Function<T, Object>... objectExtractors) {
        return logWithTransaction(message, ctx -> Collections.emptyList(), objectExtractors);
    }

    static <T> Function<T, Mono<T>> logWithTransaction(String message, Function<TransactionContext, List<Object>> ctxDataExtractor, Object... objects) {
        return type -> RequestProcessor.getContext().map(context -> {
            TraceLogger traceLogger = context.getTraceLogger();
            List<Object> ctxObjects = ctxDataExtractor.apply(context.getTransactionContext());
            traceLogger.addLogEntry(message, ctxObjects, objects);
            return type;
        });
    }

    @SafeVarargs
    static <T> Function<T, Mono<T>> logWithTransaction(String message, Function<TransactionContext, List<Object>> ctxDataExtractor, Function<T, Object>... objectExtractors) {
        return type -> RequestProcessor.getContext().map(context -> {
            TraceLogger traceLogger = context.getTraceLogger();
            List<Object> ctxObjects = ctxDataExtractor.apply(context.getTransactionContext());
            traceLogger.addLogEntry(message, ctxObjects, Arrays.stream(objectExtractors).map(oe -> oe.apply(type)).toArray());
            return type;
        });
    }
}
