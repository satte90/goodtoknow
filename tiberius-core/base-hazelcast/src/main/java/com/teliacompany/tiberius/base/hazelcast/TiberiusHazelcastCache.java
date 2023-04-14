package com.teliacompany.tiberius.base.hazelcast;

import com.hazelcast.map.IMap;
import com.teliacompany.tiberius.base.hazelcast.config.TiberiusCacheProperties;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public abstract class TiberiusHazelcastCache<K, V> {
    private static final Logger LOG = LoggerFactory.getLogger(TiberiusHazelcastCache.class);

    private IMap<K, V> cacheMap;

    //These are set by TiberiusHazelcastCacheConfigurator
    private String name;
    private TiberiusCacheProperties properties;

    protected void initialize(IMap<K, V> map) {
        this.cacheMap = map;
    }

    /**
     * Set once, and only once by TiberiusHazelcastAutoConfiguration
     *
     * @param cacheName - Name of the cache based on name and version in the annotation and a generated hashCode, hashCode is based on configuration used
     */
    public void setConfig(String cacheName, TiberiusCacheProperties props) {
        if(this.name == null) {
            this.name = cacheName;
            this.properties = props;
        } else {
            throw new IllegalStateException("Cache name has already been set!");
        }
    }

    public String getName() {
        return name;
    }

    public TiberiusCacheProperties getProperties() {
        return properties;
    }

    /**
     * Provide method to extract the key field from the value object, Used when putting values without a key parameter.
     */
    protected abstract K keyExtractor(V value);

    public int size() {
        return cacheMap.size();
    }

    public Mono<Void> putAll(List<V> values) {
        Map<K, V> customersMap = values.stream().collect(Collectors.toMap(this::keyExtractor, c -> c, (c1, c2) -> c2));
        return Mono.fromCompletionStage(cacheMap.putAllAsync(customersMap)
                .thenApply(Optional::ofNullable))
                .flatMap(o -> o.map(Mono::just).orElseGet(Mono::empty))
                .timeout(Duration.ofMillis(Math.min(30000, (values.size() + 1) * 5000L)));
    }

    public Mono<Void> putAll(Map<K, V> values) {
        return Mono.fromCompletionStage(cacheMap.putAllAsync(values)
                .thenApply(Optional::ofNullable))
                .flatMap(o -> o.map(Mono::just).orElseGet(Mono::empty))
                .timeout(Duration.ofMillis(Math.min(30000, (values.size() + 1) * 5000L)));
    }

    public Mono<V> put(K key, V value) {
        return Mono.fromCompletionStage(cacheMap.putAsync(key, value)
                .thenApply(Optional::ofNullable))
                .flatMap(o -> o.map(Mono::just).orElseGet(Mono::empty))
                .timeout(Duration.ofMillis(5000));
    }

    public Mono<V> put(V value) {
        return put(keyExtractor(value), value);
    }

    public Mono<V> get(K key) {
        return Mono.fromCompletionStage(cacheMap.getAsync(key)
                .thenApply(Optional::ofNullable))
                .flatMap(o -> o.map(Mono::just).orElseGet(Mono::empty))
                .timeout(Duration.ofMillis(1000))
                .onErrorResume(e -> e instanceof TimeoutException ? Mono.empty() : Mono.error(e));
    }

    public Mono<V> get(K key, long timeoutMs) {
        return Mono.fromCompletionStage(cacheMap.getAsync(key)
                .thenApply(Optional::ofNullable))
                .flatMap(o -> o.map(Mono::just).orElseGet(Mono::empty))
                .timeout(Duration.ofMillis(timeoutMs))
                .onErrorResume(e -> e instanceof TimeoutException ? Mono.empty() : Mono.error(e));
    }

    public Mono<Map<K, V>> getAll() {
        return getAll(cacheMap.keySet());
    }

    public Mono<Map<K, V>> getAll(Collection<K> keys) {
        List<Mono<Pair<K, V>>> listOfMonos = keys.stream()
                .map(key -> get(key).map(v -> Pair.of(key, v)))
                .collect(Collectors.toList());

        return Flux.fromIterable(listOfMonos)
                .flatMap(l -> l)
                .collectList()
                .map(listOfPairs -> listOfPairs.stream()
                        .collect(Collectors.toMap(Pair::getKey, Pair::getValue, (p1, p2) -> p2))
                );
    }

    public Mono<V> remove(K key) {
        return Mono.fromCompletionStage(cacheMap.removeAsync(key)
                .thenApply(Optional::ofNullable))
                .flatMap(o -> o.map(Mono::just).orElseGet(Mono::empty))
                .timeout(Duration.ofMillis(1000))
                .onErrorResume(e -> e instanceof TimeoutException ? Mono.empty() : Mono.error(e));
    }

    public void clear() {
        cacheMap.clear();
    }

    public IMap<K, V> getInternalMap() {
        return cacheMap;
    }
}
