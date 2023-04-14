package com.teliacompany.tiberius.base.hazelcast.config;

import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionConfig;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.RestApiConfig;
import com.hazelcast.config.RestEndpointGroup;
import com.hazelcast.config.UserCodeDeploymentConfig;
import com.teliacompany.webflux.error.exception.server.InternalServerErrorException;
import com.teliacompany.tiberius.base.hazelcast.TiberiusHazelcastCache;
import com.teliacompany.tiberius.base.hazelcast.TiberiusHazelcastCacheRegistry;
import com.teliacompany.tiberius.base.hazelcast.TiberiusHazelcastComponent;
import com.teliacompany.tiberius.base.hazelcast.controller.CacheTestSupportController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@EnableCaching
@Configuration
@ComponentScan(basePackageClasses = {CacheTestSupportController.class, TiberiusHazelcastCacheRegistry.class})
@Import({HazelcastVersionProperties.class, TiberiusHazelcastProperties.class})
public class TiberiusHazelcastAutoConfiguration extends CachingConfigurerSupport {
    private static final Logger LOG = LoggerFactory.getLogger(TiberiusHazelcastAutoConfiguration.class);
    private final List<TiberiusHazelcastConfigurator> hazelcastMapConfigurators;
    private final TiberiusHazelcastProperties tiberiusHazelcastProperties;

    public TiberiusHazelcastAutoConfiguration(List<TiberiusHazelcastConfigurator> hazelcastMapConfigurators, TiberiusHazelcastProperties tiberiusHazelcastProperties) {
        this.hazelcastMapConfigurators = hazelcastMapConfigurators;
        this.tiberiusHazelcastProperties = tiberiusHazelcastProperties;
        LOG.info("Hazelcast instanceName: {}", this.tiberiusHazelcastProperties.getInstanceName());
    }

    @Bean
    public Config hazelCastConfig(List<TiberiusHazelcastCache<?, ?>> caches) {
        boolean inKubernetes = new File(tiberiusHazelcastProperties.getTokenFilePath()).exists();

        Config config = new Config(this.tiberiusHazelcastProperties.getInstanceName())
                .setClusterName(this.tiberiusHazelcastProperties.getClusterName())
                .setClassLoader(Thread.currentThread().getContextClassLoader())
                .setProperty("hazelcast.phone.home.enabled", "false")
                .setProperty("hazelcast.health.monitoring.level", "SILENT")
                .setProperty("hazelcast.health.monitoring.delay.seconds", "30")
                .setProperty("hazelcast.wait.seconds.before.join", "10")
                .setProperty("hazelcast.max.wait.seconds.before.join", "10")
                .setProperty("hazelcast.connect.all.wait.seconds", "20")
                .setProperty("hazelcast.operation.call.timeout.millis", "5000")
                .setProperty("hazelcast.socket.connect.timeout.seconds", "4")
                .setProperty("hazelcast.heartbeat.failuredetector.type", "deadline")
                .setProperty("hazelcast.heartbeat.interval.seconds", "5")
                .setProperty("hazelcast.max.no.heartbeat.seconds", "40")
                .setProperty("hazelcast.invocation.max.retry.count", "5")
                .setProperty("hazelcast.operation.fail.on.indeterminate.state", "true");

        // "Network Partitioning (Split-Brain Syndrome)", see https://diva.teliacompany.net/bitbucket/projects/DCCONTEXT/repos/web-analytics/browse/web-analytics-service/src/main/java/com/teliacompany/web/analytics/service/config/HazelcastConfig.java#53
        config.setProperty("hazelcast.merge.first.run.delay.seconds", "60"); // Default is 300 seconds.
        config.setProperty("hazelcast.merge.next.run.delay.seconds", "60"); // Default is 120 seconds.

        //Disable metrics, may be cause for memory leak according to: https://github.com/hazelcast/hazelcast/issues/16672
        config.getMetricsConfig().setEnabled(false);

        final UserCodeDeploymentConfig distCLConfig = config.getUserCodeDeploymentConfig();
        distCLConfig.setEnabled(true)
                .setClassCacheMode(UserCodeDeploymentConfig.ClassCacheMode.ETERNAL)
                .setProviderMode(UserCodeDeploymentConfig.ProviderMode.LOCAL_CLASSES_ONLY);

        NetworkConfig network = config.getNetworkConfig();
        network.setRestApiConfig(getRestApiConfig());

        // Auto-discovery
        JoinConfig joinConfig = network.getJoin();
        joinConfig.getMulticastConfig().setEnabled(false);

        if(!tiberiusHazelcastProperties.isDiscoveryEnabled()) {
            LOG.info("Hazelcast discovery disabled!");
            joinConfig.getTcpIpConfig().setEnabled(false);
            config.setProperty("hazelcast.discovery.enabled", "false");
        } else {
            LOG.info("Hazelcast discovery enabled!");
            if(inKubernetes) {
                config.setProperty("hazelcast.discovery.enabled", "true");
                joinConfig.getKubernetesConfig()
                        .setEnabled(true)
                        .setProperty("namespace", "tse")
                        .setProperty("service-name", this.tiberiusHazelcastProperties.getInstanceName())
                        .setProperty("api-token", getToken())
                        .setProperty("service-port", "5701");

                LOG.info("Running in Kubernetes. namespace: {}, service-name: {}", joinConfig.getKubernetesConfig().getProperty("namespace"),
                        joinConfig.getKubernetesConfig().getProperty("service-name"));
            }
        }

        this.hazelcastMapConfigurators.forEach(c -> c.configure(config));

        configureCacheMaps(config, caches);

        return config;
    }

    private RestApiConfig getRestApiConfig() {
        return new RestApiConfig()
                .setEnabled(this.tiberiusHazelcastProperties.isLocalMode() || this.tiberiusHazelcastProperties.isTestMode())
                .enableGroups(RestEndpointGroup.HEALTH_CHECK, RestEndpointGroup.CLUSTER_READ);
    }

    private String getToken() {
        try(Stream<String> stream = Files.lines(Paths.get(tiberiusHazelcastProperties.getTokenFilePath()))) {
            return stream.findFirst().orElse("");
        } catch(IOException e) {
            LOG.error("Could not read Kubernetes service account token", e);
            throw new InternalServerErrorException("Could not read Kubernetes service account token");
        }
    }

    private void configureCacheMaps(Config config, List<TiberiusHazelcastCache<?, ?>> caches) {
        Map<String, TiberiusHazelcastCache<?, ?>> cacheMap = new HashMap<>();

        caches.forEach(cache -> {
            if(!cache.getClass().isAnnotationPresent(TiberiusHazelcastComponent.class)) {
                throw new InternalServerErrorException("Bean " + cache.getClass().getName() + " must have @TiberiusHazelcastComponent annotation!");
            } else {
                TiberiusHazelcastComponent annotation = cache.getClass().getAnnotation(TiberiusHazelcastComponent.class);
                final TiberiusCacheProperties props = new TiberiusCacheProperties(annotation);

                //Generate a hashCode of the properties - If properties change we need a new name and a new map, otherwise we will just connect to the existing one with
                // old configuration: https://github.com/hazelcast/hazelcast/issues/10872 %
                final String hashHexCode = Integer.toHexString(props.hashCode());

                final String cacheName = annotation.name() + "_v" + annotation.version() + "_" + hashHexCode;

                if(cacheMap.containsKey(cacheName)) {
                    throw new InternalServerErrorException("Multiple caches with the same name is not allowed (even if version differs)");
                }

                cache.setConfig(cacheName, props);
                LOG.info("Configuring cache map for {}", cacheName);

                final MapConfig mapConfig = new MapConfig(cacheName)
                        .setTimeToLiveSeconds(props.getTimeToLiveSeconds())
                        .setMaxIdleSeconds(props.getMaxIdleSeconds())
                        .setEvictionConfig(new EvictionConfig()
                                .setEvictionPolicy(props.getEvictionPolicy())
                                .setMaxSizePolicy(props.getMaxSizePolicy())
                                .setSize(props.getMaxSize())
                        );
                config.addMapConfig(mapConfig);

                //Store cache as named without version suffix
                cacheMap.put(cacheName, cache);
                LOG.info("Initialized hazelcast cache for {}", cacheName);
            }
        });
    }
}
