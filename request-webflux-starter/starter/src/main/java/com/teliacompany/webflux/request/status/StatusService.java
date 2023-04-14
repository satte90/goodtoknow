package com.teliacompany.webflux.request.status;

import com.teliacompany.webflux.request.client.WebClientRegistry;
import com.teliacompany.webflux.request.status.model.PingableService;
import com.teliacompany.webflux.request.RequestProcessor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StatusService implements ApplicationListener<ContextRefreshedEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(StatusService.class);
    private List<PingableService> integrationsToPing;

    @Value("${status.ping.timeout:5000}")
    private int pingTimeout;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if(integrationsToPing == null) {
            this.integrationsToPing = WebClientRegistry.stream()
                    .map(wc -> {
                        String host = StringUtils.prependIfMissingIgnoreCase(wc.getHost(), "http://", "https://");
                        URI uri = URI.create(host);
                        Integer port = getPort(uri.getPort(), host);
                        return new PingableService(wc.getServiceName(), uri.getHost(), port, wc.getProxyHost(), wc.getProxyPort());
                    })
                    .collect(Collectors.toList());
        }
    }

    public Mono<String> ping() {
        return Mono.just("pong");
    }

    public Mono<List<PingableService>> deepPing() {
        // TODO: Add some synchronized stuff around this. Dont allow multiple deep ping calls at the same time, rather let them wait for the first to finish,
        // then return that status...
        // integrationsToPing will contain the previous status.
        // Todo: Special handling for apiMarket services?

        return Flux.fromIterable(integrationsToPing)
                .flatMap(pingableService -> RequestProcessor.scheduleBlocking(() -> deepPingService(pingableService)))
                .collectList();
    }

    private PingableService deepPingService(PingableService pingableService) {
        LOG.debug("Pinging {} on host {} and port {}...", pingableService.getServiceName(), pingableService.getHost(), pingableService.getPort());
        final Pinger.Response pingResponse = Pinger.ping(pingableService.getServiceName(), pingableService.getHost(), pingableService.getPort(), pingTimeout, pingableService.getProxy(), pingableService.getProxyPort());
        pingableService.setStatus(pingResponse.getStatus());
        pingableService.setResponseTime(pingResponse.getResponseTime());
        if(pingResponse.getException() != null) {
            pingableService.setErrorMessage(pingResponse.getException().getClass().getSimpleName() + ": " + pingResponse.getException().getMessage());
        } else {
            pingableService.setErrorMessage(null);
        }
        return pingableService;
    }

    private static Integer getPort(int port, String host) {
        if(port > 0) {
            return port;
        }
        if(StringUtils.startsWith(host, "https")) {
            return 443;
        } else if(StringUtils.startsWith(host, "http")) {
            return 80;
        }
        return port;
    }
}
