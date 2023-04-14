package com.teliacompany.tiberius.base.utils;

import com.teliacompany.webflux.error.exception.server.InternalServerErrorException;
import com.teliacompany.webflux.request.client.WebClientResponse;

import java.util.function.Function;

public final class BodyExtractorUtils {
    private BodyExtractorUtils() {
        //Util class, shall not be instantiated
    }

    public static <T> Function<WebClientResponse<T>, T> extractBody(String serviceName) {
        return response  -> response.getBody().orElseThrow(() -> new InternalServerErrorException("Received empty response", serviceName, "Empty response"));
    }
}
