package com.teliacompany.webflux.request.client;

import com.teliacompany.webflux.request.metrics.MetricsReporter;
import com.teliacompany.webflux.request.log.RequestLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder.Resilience4JCircuitBreakerConfiguration;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

public class WebClientInitializer implements ApplicationListener<ContextRefreshedEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(WebClientInitializer.class);
    private final MetricsReporter metricsReporter;
    private final ReactiveCircuitBreakerFactory<Resilience4JCircuitBreakerConfiguration, Resilience4JConfigBuilder> reactiveCircuitBreakerFactory;
    private final RequestLogger requestLogger;
    private boolean clientsInitialized = false;

    // Allow access to "itself" in a static context when application has started
    private static WebClientInitializer instance = null;

    public WebClientInitializer(RequestLogger requestLogger, MetricsReporter metricsReporter, ReactiveCircuitBreakerFactory<Resilience4JCircuitBreakerConfiguration, Resilience4JConfigBuilder> reactiveCircuitBreakerFactory) {
        this.requestLogger = requestLogger;
        this.metricsReporter = metricsReporter;
        this.reactiveCircuitBreakerFactory = reactiveCircuitBreakerFactory;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        if(!clientsInitialized) {
            WebClientRegistry.stream().forEach(wc -> wc.init(requestLogger, metricsReporter, reactiveCircuitBreakerFactory));
            clientsInitialized = true;
            LOG.info("Web clients initiated...");
            setInstance(this);
        }
    }

    private static void setInstance(WebClientInitializer webClientInitializer) {
        if(instance == null) {
            instance = webClientInitializer;
        }
    }

    /**
     * Force initialization of a webclient if it has not been done already. All clients created as beans, i.e. created during application boot will be initialized
     * automatically. Use this only for dynamically created WebClients after application has fully started.
     *
     * Only initializes clients when:
     * .-The "onApplicationStarted" initialization has been executed already (instance is no longer null)
     * .-The client  has not been initialized
     */
    public static void initializeWebClient(WebClient webClient) {
        if(instance != null && !webClient.isInitialized()) {
            webClient.init(instance.requestLogger, instance.metricsReporter, instance.reactiveCircuitBreakerFactory);
        }
    }
}
