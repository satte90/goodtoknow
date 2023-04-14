package com.teliacompany.webflux.request.processor;

import reactor.core.publisher.Mono;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class RequestHandler<I, J, IC, O, OC> {
    private final BiFunction<IC, J, Mono<O>> biHandler;
    private final BiFunction<I, J, IC> biInputConverter;
    private final Function<O, OC> outputConverter;

    public RequestHandler(Supplier<Mono<O>> handler, Function<O, OC> outputConverter) {
        this.biHandler = (v, u) -> handler.get();
        this.biInputConverter = (i, j) -> null;
        this.outputConverter = outputConverter;
    }

    public RequestHandler(Function<IC, Mono<O>> handler, Function<I, IC> inputConverter, Function<O, OC> outputConverter) {
        this.biHandler = (ic, j) -> handler.apply(ic);
        this.biInputConverter = (i, j) -> inputConverter.apply(i);
        this.outputConverter = outputConverter;
    }

    public RequestHandler(BiFunction<IC, J, Mono<O>> handler, BiFunction<I, J, IC> inputConverter, Function<O, OC> outputConverter) {
        this.biHandler = handler;
        this.biInputConverter = inputConverter;
        this.outputConverter = outputConverter;
    }

    public RequestHandler(Function<IC, Mono<O>> handler, BiFunction<I, J, IC> inputConverter, Function<O, OC> outputConverter) {
        this.biHandler = (ic, j) -> handler.apply(ic);
        this.biInputConverter = inputConverter;
        this.outputConverter = outputConverter;
    }

    public RequestHandler(BiFunction<IC, J, Mono<O>> handler, Function<I, IC> inputConverter, Function<O, OC> outputConverter) {
        this.biHandler = handler;
        this.biInputConverter = (i, j) -> inputConverter.apply(i);
        this.outputConverter = outputConverter;
    }

    public Mono<O> applyHandler(IC inputConverted, J jinput) {
        return biHandler.apply(inputConverted, jinput);
    }

    public IC convertInput(I input, J jinput) {
        return biInputConverter.apply(input, jinput);
    }

    public OC convertOutput(O output) {
        return outputConverter.apply(output);
    }
}
