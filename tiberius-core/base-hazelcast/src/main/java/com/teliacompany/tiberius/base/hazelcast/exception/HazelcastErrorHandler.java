package com.teliacompany.tiberius.base.hazelcast.exception;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.teliacompany.tiberius.base.hazelcast.config.TiberiusHazelcastProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.reactive.handler.WebFluxResponseStatusExceptionHandler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Order(100)
@ControllerAdvice
public class HazelcastErrorHandler extends WebFluxResponseStatusExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(HazelcastErrorHandler.class);
    private final String instanceName;

    public HazelcastErrorHandler(TiberiusHazelcastProperties hazelcastProperties) {
        this.instanceName = hazelcastProperties.getInstanceName();
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        if(ex instanceof HazelcastInstanceNotActiveException) {
            exchange.getResponse().setStatusCode(HttpStatus.GONE);
            restartHazelcastInstance();
            // marks the response as complete and forbids writing to it
            return exchange.getResponse().setComplete();
        }

        return super.handle(exchange, ex);
    }

    private void restartHazelcastInstance() {
        HazelcastInstance instance = Hazelcast.getHazelcastInstanceByName(instanceName);
        if(instance != null) {
            LOG.info("Restarting hazelcast for {}", instanceName);
            Config config = instance.getConfig();
            instance.shutdown();
            Hazelcast.newHazelcastInstance(config);
        } else {
            LOG.info("Cannot restarting hazelcast for {} as it did not exist in the first place...", instanceName);
        }
    }
}
