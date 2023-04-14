package com.teliacompany.tiberius.base.hazelcast.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teliacompany.webflux.error.exception.client.BadRequestException;
import com.teliacompany.webflux.error.exception.client.NotFoundException;
import com.teliacompany.webflux.request.processor.RestRequestProcessor;
import com.teliacompany.tiberius.base.hazelcast.TiberiusHazelcastCache;
import com.teliacompany.tiberius.base.hazelcast.TiberiusHazelcastCacheRegistry;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@RestController
@RequestMapping("testsupport/hazelcast")
@Profile({"componenttest", "local"})
@Hidden
public class CacheTestSupportController {
    private final RestRequestProcessor requestProcessor;
    private final TiberiusHazelcastCacheRegistry cacheRegistry;
    private final ObjectMapper objectMapper;

    public CacheTestSupportController(RestRequestProcessor restRequestProcessor, TiberiusHazelcastCacheRegistry cacheRegistry, ObjectMapper objectMapper) {
        this.requestProcessor = restRequestProcessor;
        this.cacheRegistry = cacheRegistry;
        this.objectMapper = objectMapper;
    }

    @DeleteMapping("{cacheName}")
    public Mono<ResponseEntity<Object>> clearCache(ServerHttpRequest serverHttpRequest, @PathVariable String cacheName) {
        return requestProcessor.process(serverHttpRequest)
                .withoutRequestBody()
                .withHandler(() -> {
                    final TiberiusHazelcastCache<Object, Object> cacheForName = findCacheStartingWithName(cacheName);
                    cacheForName.clear();
                    return Mono.empty();
                });
    }

    @SuppressWarnings("BlockingMethodInNonBlockingContext")
    @PutMapping("{cacheName}/{id}/{className}")
    public Mono<ResponseEntity<Object>> clearCache(ServerHttpRequest serverHttpRequest, @PathVariable String cacheName, @PathVariable String id, @PathVariable String className) {
        return requestProcessor.process(serverHttpRequest)
                .withRequestBody(String.class)
                .withHandler(json -> {
                    try {
                        Object o = objectMapper.readValue(json, Class.forName(className));
                        final TiberiusHazelcastCache<Object, Object> cacheForName = findCacheStartingWithName(cacheName);
                        return cacheForName.put(id, o).then();
                    } catch(JsonProcessingException | ClassNotFoundException e) {
                        throw new BadRequestException("Cannot put value in cache", "Internal", e);
                    }
                });
    }

    /**
     * Find cache starting with name or throw not found exception
     */
    private TiberiusHazelcastCache<Object, Object> findCacheStartingWithName(String cacheName) {
        return cacheRegistry.getCaches().stream()
                .filter(cache -> cache.getName().startsWith(cacheName))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Cache for name " + cacheName + " was not found"));
    }

}
