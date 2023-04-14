package com.teliacompany.webflux.request;

import org.springframework.http.HttpHeaders;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

@FunctionalInterface
public interface TransactionMetaDataProducer<I> extends BiFunction<I, HttpHeaders, Map<String, String>> {

    static TransactionMetaDataProducer<Void> empty() {
        return (i, h) -> new HashMap<>();
    }
}
