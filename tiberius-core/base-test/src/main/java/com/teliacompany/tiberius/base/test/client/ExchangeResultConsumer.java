package com.teliacompany.tiberius.base.test.client;

import com.teliacompany.tiberius.base.utils.TiberiusObjectMapper;
import com.teliacompany.webflux.error.api.ErrorResponse;
import org.springframework.test.web.reactive.server.EntityExchangeResult;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public final class ExchangeResultConsumer {
    private ExchangeResultConsumer() {
        // Hidden by design
    }

    public static <T> Builder<T> forType(Class<T> type) {
        return new Builder<>(type);
    }

    public static Builder<ErrorResponse> forError() {
        return new Builder<>(ErrorResponse.class);
    }

    public static class Builder<T> {
        private final Class<T> type;
        private final List<ConsumerWithExpectation<T>> consumersWithExpectation;
        private final List<Consumer<T>> consumers;

        private Builder(Class<T> type) {
            this.type = type;
            consumersWithExpectation = new ArrayList<>();
            consumers = new ArrayList<>();
        }

        public final Builder<T> withConsumer(BiConsumer<T, T> consumer, T expectedResult) {
            this.consumersWithExpectation.add(new ConsumerWithExpectation<>(consumer, expectedResult));
            return this;
        }

        public final Builder<T> withConsumer(Consumer<T> consumer) {
            this.consumers.add(consumer);
            return this;
        }

        public final Consumer<EntityExchangeResult<byte[]>> build() {
            return entityExchangeResult -> {
                assertNotNull(entityExchangeResult);
                assertNotNull(entityExchangeResult.getResponseBody());

                T response;
                if(type.equals(String.class)) {
                    response = (T) new String(entityExchangeResult.getResponseBody(), Charset.forName("UTF-8"));
                } else {
                    response = TiberiusObjectMapper.read(entityExchangeResult.getResponseBody(), type);
                }

                this.consumersWithExpectation.forEach(consumerWithExpectation -> consumerWithExpectation.consumer.accept(response, consumerWithExpectation.expected));
                this.consumers.forEach(consumer -> consumer.accept(response));
            };
        }
    }

    static class ConsumerWithExpectation<T> {
        private final BiConsumer<T, T> consumer;
        private final T expected;

        ConsumerWithExpectation(BiConsumer<T, T> consumer, T expected) {
            this.consumer = consumer;
            this.expected = expected;
        }
    }


}
