package com.teliacompany.tiberius.base.server.integration.praefectus;

import com.teliacompany.webflux.request.client.WebClient;
import com.teliacompany.webflux.request.client.WebClientBuilder;
import com.teliacompany.webflux.request.client.WebClientConfig;
import com.teliacompany.webflux.request.log.RequestLoggingOptions;
import com.teliacompany.tiberius.base.server.api.ServiceRegistryEntry;
import com.teliacompany.tiberius.base.server.config.RegistrationProperties;
import org.apache.logging.log4j.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class PraefectusClient {
    private static final Logger LOG = LoggerFactory.getLogger(PraefectusClient.class);
    private final WebClient webClient;

    public PraefectusClient(RegistrationProperties registrationProperties) {
        if(registrationProperties.getServiceRegistrationHost() != null) {
            WebClientConfig config = WebClientConfig.builder()
                    .withServiceName(registrationProperties.getServiceRegistrationName())
                    .withHost(registrationProperties.getServiceRegistrationHost())
                    .withBasePath(registrationProperties.getServiceRegistrationPath())
                    .withLoggingOptions(RequestLoggingOptions.defaults().setLogLevel(Level.DEBUG))
                    .build();
            webClient = WebClientBuilder.withConfig(config).build();
        } else {
            LOG.warn("No service registration configured. Cannot register with praefectus");
            webClient = null;
        }
    }

    public Mono<String> register(ServiceRegistryEntry entry) {
        if(webClient != null) {
            return webClient.put("")
                    .body(entry)
                    .retrieve()
                    .timeout(Duration.ofSeconds(10))
                    .map(voidWebClientResponse -> "OK")
                    .onErrorResume(e -> {
                        LOG.warn("Could not register service: {}", e.getMessage());
                        return Mono.just("NOK");
                    });
        }
        return Mono.just("Ignored");
    }

    public Mono<String> unregister(String hostName) {
        if(webClient != null) {
            return webClient.delete("/{hostName}")
                    .uriVariable("hostName", hostName)
                    .retrieve()
                    .timeout(Duration.ofSeconds(10))
                    .map(voidWebClientResponse -> "OK")
                    .onErrorResume(e -> Mono.just("NOK"));
        }
        return Mono.just("Ignored");
    }
}
