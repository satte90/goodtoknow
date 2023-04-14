package com.teliacompany.webflux.request.utils;

import com.teliacompany.webflux.request.context.TransactionContext;
import org.springframework.http.HttpHeaders;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class TransactionUtils {
    private TransactionUtils() {
    }

    public static void addMetaDataFromHeaders(List<String> incomingMetaData, Map<String, String> metaData) {
        incomingMetaData.stream()
                .filter(Objects::nonNull)
                .forEach(iMetaDataEntry -> {
                    String[] iMetaDataArray = iMetaDataEntry.split("=", 2);
                    if(iMetaDataArray.length == 2) {
                        // Don't overwrite user-added meta data from
                        metaData.putIfAbsent(iMetaDataArray[0], iMetaDataArray[1]);
                    }
                });
    }

    public static List<String> getMetaDataAsHeaderValue(TransactionContext transactionContext) {
        return transactionContext.getMetaData().entrySet().stream()
                .filter(Objects::nonNull)
                .filter(metaData -> metaData.getValue() != null)
                .filter(metaData -> metaData.getValue().length() < 51)
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.toList());
    }

    public static String getTransactionIdFromHeaders(HttpHeaders headers) {
        return getTransactionIdFromHeaders(headers, () -> UUID.randomUUID().toString());
    }

    public static String getTransactionIdFromHeaders(HttpHeaders headers, Supplier<String> defaultValueSupplier) {
        if(headers.getFirst(Constants.HTTP_TRANSACTION_ID_HEADER) != null) {
            return headers.getFirst(Constants.HTTP_TRANSACTION_ID_HEADER);
        }
        if(headers.getFirst(Constants.HTTP_REQUEST_ID_HEADER) != null) {
            return headers.getFirst(Constants.HTTP_REQUEST_ID_HEADER);
        }

        if(headers.getFirst(Constants.HTTP_CORRELATION_ID_HEADER) != null) {
            return headers.getFirst(Constants.HTTP_CORRELATION_ID_HEADER);
        }
        return defaultValueSupplier.get();
    }

    public static String getTcadFromHeaders(HttpHeaders headers, Supplier<String> defaultValueSupplier) {
        if(headers.getFirst(Constants.HTTP_TELIA_TCAD) != null) {
            return headers.getFirst(Constants.HTTP_TELIA_TCAD);
        }
        if(headers.getFirst(Constants.HTTP_X_TCAD) != null) {
            return headers.getFirst(Constants.HTTP_X_TCAD);
        }
        return defaultValueSupplier.get();
    }

    public static String getTscIdFromHeaders(HttpHeaders headers, Supplier<String> defaultValueSupplier) {
        if(headers.getFirst(Constants.HTTP_X_TSCID) != null) {
            return headers.getFirst(Constants.HTTP_X_TSCID);
        }
        return defaultValueSupplier.get();
    }
}
