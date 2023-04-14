package com.teliacompany.tiberius.base.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings({"SpringJavaInjectionPointsAutowiringInspection", "unused"})
@Component("TiberiusHazelcastCacheRegistry")
public class TiberiusHazelcastCacheRegistry {
    private static final Logger LOG = LoggerFactory.getLogger(TiberiusHazelcastCacheRegistry.class);
    private final Map<String, TiberiusHazelcastCache<?, ?>> caches;

    /**
     * When Spring Boot has created a Hazelcast instance (with Config bean created in TiberiusHazelcastCacheConfigurator) Initialize all TiberiusHazelcastCaches
     * I.e. set the map contained in the TiberiusHazelcastCache to be the actual Hazelcast map
     *
     * @param hazelcastInstance - The Hazelcast instance, created by Spring Boot
     * @param caches            - TiberiusHazelcastCaches
     */
    public TiberiusHazelcastCacheRegistry(HazelcastInstance hazelcastInstance, List<TiberiusHazelcastCache<?, ?>> caches) {
        this.caches = new HashMap<>();

        caches.forEach(cache -> {
            LOG.info("Initializing hazelcast cache: {}", cache.getName());
            cache.initialize(hazelcastInstance.getMap(cache.getName()));
            this.caches.put(cache.getName(), cache);
        });
    }

    @SuppressWarnings("unchecked")
    public TiberiusHazelcastCache<Object, Object> getCache(String name) {
        return (TiberiusHazelcastCache<Object, Object>) caches.get(name);
    }

    @SuppressWarnings("unchecked")
    public List<TiberiusHazelcastCache<Object, Object>> getCaches() {
       return caches.values().stream().map(c -> (TiberiusHazelcastCache<Object, Object>) c).collect(Collectors.toList());
    }
}
