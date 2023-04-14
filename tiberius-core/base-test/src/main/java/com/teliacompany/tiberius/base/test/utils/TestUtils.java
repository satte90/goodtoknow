package com.teliacompany.tiberius.base.test.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.teliacompany.tiberius.base.test.exception.TestRuntimeException;
import com.teliacompany.webflux.jackson.TeliaObjectMapper;
import com.teliacompany.webflux.request.context.RequestContextBuilder;
import com.teliacompany.webflux.request.context.TransactionContext;
import com.teliacompany.webflux.request.log.trace.TraceLogger;
import org.apache.commons.io.IOUtils;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

import static com.teliacompany.webflux.request.utils.Constants.TRANSACTION_CONTEXT_KEY;

public final class TestUtils {
    public static final ObjectMapper TEST_OBJECT_MAPPER = getObjectMapper();
    public static final ObjectMapper TEST_SORTED_OBJECT_MAPPER = getObjectMapper()
            .copy()
            .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
            .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);


    private TestUtils() {
        //Util class
    }

    private static ObjectMapper getObjectMapper() {
        return TeliaObjectMapper.get()
                .setDefaultPrettyPrinter(new TiberiusApprovalsPrettyPrinter());
    }

    public static String asJson(Object o) {
        try {
            return TEST_OBJECT_MAPPER.writeValueAsString(o);
        } catch(JsonProcessingException e) {
            throw new TestRuntimeException(e);
        }
    }

    public static String asPrettyJson(Object o) {
        return asPrettyJson(o, true);
    }

    public static String asPrettyJson(Object o, boolean sorted) {
        try {
            final ObjectMapper mapper = sorted ? TEST_SORTED_OBJECT_MAPPER : TEST_OBJECT_MAPPER;
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(o);
        } catch(JsonProcessingException e) {
            throw new TestRuntimeException(e);
        }
    }

    public static String readJsonFile(String path) {
        try(InputStream is = TestUtils.class.getClassLoader().getResourceAsStream(path)) {
            assert is != null;
            return IOUtils.toString(is, StandardCharsets.UTF_8);
        } catch(Exception e) {
            throw new TestRuntimeException("Couldn't read or find: " + path, e);
        }
    }

    public static <T> T testMono(Supplier<Mono<T>> supplier) {
        final TransactionContext transactionContext = new RequestContextBuilder()
                .buildTransactionContext();

        return supplier.get()
                .contextWrite(ctx -> ctx.put(TRANSACTION_CONTEXT_KEY, transactionContext).put(TraceLogger.class, TraceLogger.create(false)))
                .block();
    }
}
