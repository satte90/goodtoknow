package com.teliacompany.webflux.request.processor.model;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public class RequestBody<I, J> {
    private final Class<I> requestBodyClass;
    private final I requestObject1;
    private final J requestObject2;

    public RequestBody(Class<I> requestBodyClass, I requestObject1, J requestObject2) {
        this.requestBodyClass = requestBodyClass;
        this.requestObject1 = requestObject1;
        this.requestObject2 = requestObject2;
    }

    @Nullable
    public I getRequestObject1() {
        return requestObject1;
    }

    @Nullable
    public J getRequestObject2() {
        return requestObject2;
    }

    @NonNull
    public Class<I> getRequestBodyClass() {
        return requestBodyClass;
    }
}
