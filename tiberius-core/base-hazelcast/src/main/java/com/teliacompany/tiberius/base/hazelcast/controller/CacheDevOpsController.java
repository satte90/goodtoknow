package com.teliacompany.tiberius.base.hazelcast.controller;

import com.hazelcast.core.HazelcastInstance;
import com.teliacompany.webflux.request.processor.RestRequestProcessor;
import com.teliacompany.tiberius.base.hazelcast.TiberiusHazelcastCacheRegistry;
import com.teliacompany.tiberius.base.hazelcast.api.CacheStatus;
import com.teliacompany.tiberius.base.hazelcast.config.HazelcastVersionProperties;
import com.teliacompany.tiberius.base.hazelcast.config.TiberiusHazelcastProperties;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@RestController
@RequestMapping("devops/hazelcast")
@Hidden
public class CacheDevOpsController {
    private final RestRequestProcessor requestProcessor;
    private final TiberiusHazelcastCacheRegistry cacheRegistry;
    private final HazelcastInstance hazelcastInstance;
    private final TiberiusHazelcastProperties properties;

    public CacheDevOpsController(RestRequestProcessor restRequestProcessor, TiberiusHazelcastCacheRegistry cacheRegistry, HazelcastInstance hazelcastInstance, TiberiusHazelcastProperties properties) {
        this.requestProcessor = restRequestProcessor;
        this.cacheRegistry = cacheRegistry;
        this.hazelcastInstance = hazelcastInstance;
        this.properties = properties;
    }

    @Profile({"local", "dev", "sit", "at"}) //Don't expose data in prod/beta
    @GetMapping
    public Mono<ResponseEntity<Object>> getStats(ServerHttpRequest serverHttpRequest) {
        return requestProcessor.process(serverHttpRequest)
                .withoutRequestBody()
                .withHandler(() -> {
                    Map<String, Object> response = new HashMap<>();

                    response.put("Instance Name", hazelcastInstance.getName());
                    response.put("Cluster Name", hazelcastInstance.getConfig().getClusterName());
                    response.put("Properties", hazelcastInstance.getConfig().getProperties());
                    response.put("Map Configs", hazelcastInstance.getConfig().getMapConfigs());
                    response.put("Advanced Network Config", hazelcastInstance.getConfig().getAdvancedNetworkConfig());
                    response.put("Member Attribute Config", hazelcastInstance.getConfig().getMemberAttributeConfig());
                    response.put("Cluster State", hazelcastInstance.getCluster().getClusterState().name());
                    response.put("Cluster Version", hazelcastInstance.getCluster().getClusterVersion());
                    response.put("Cluster Time", hazelcastInstance.getCluster().getClusterTime());
                    response.put("Cluster Local Member", hazelcastInstance.getCluster().getLocalMember());
                    response.put("Cluster Members", hazelcastInstance.getCluster().getMembers().stream().map(m -> m.getAddress().getHost()).collect(Collectors.joining(", ")));

                    final HazelcastVersionProperties v = properties.getVersions();
                    response.put("Versions", Map.of(
                            "Hazelcast", v.getHazelcastVersion(),
                            "Hazelcast Spring", v.getHazelcastSpringVersion(),
                            "Hazelcast Kubernetes", v.getHazelcastKubernetesVersion()
                    ));
                    return Mono.just(response);
                });
    }

    @GetMapping("cache")
    public Mono<ResponseEntity<Object>> getCacheStats(ServerHttpRequest serverHttpRequest) {
        return requestProcessor.process(serverHttpRequest)
                .withoutRequestBody()
                .withHandler(() -> {
                    List<CacheStatus> cacheStatuses = cacheRegistry.getCaches().stream().map(cache -> {
                        CacheStatus cacheStatus = new CacheStatus();
                        cacheStatus.setName(cache.getName());
                        cacheStatus.setNumberOfEntries(cache.size());
                        cacheStatus.setTimeToLiveSeconds(cache.getProperties().getTimeToLiveSeconds());
                        cacheStatus.setMaxIdleSeconds(cache.getProperties().getMaxIdleSeconds());
                        cacheStatus.setEvictionPolicy(cache.getProperties().getEvictionPolicy().name());
                        cacheStatus.setMaxSizePolicy(cache.getProperties().getMaxSizePolicy().name());
                        cacheStatus.setMaxSize(cache.getProperties().getMaxSize());
                        return cacheStatus;
                    }).collect(Collectors.toList());

                    return Mono.just(cacheStatuses);
                });
    }

}
