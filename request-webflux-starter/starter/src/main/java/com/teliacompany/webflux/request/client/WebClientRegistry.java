package com.teliacompany.webflux.request.client;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Holds a registry of all instantiated WebClients
 */
public final class WebClientRegistry {
    private static final List<WebClient> REGISTRY = new ArrayList<>();

    private WebClientRegistry() {
        //Hidden by design
    }

    public static void register(WebClient webClient) {
        REGISTRY.add(webClient);
    }

    public static List<WebClient> get(String serviceName) {
        return REGISTRY.stream().filter(wc -> wc.getServiceName().equalsIgnoreCase(serviceName)).collect(Collectors.toList());
    }

    public static Stream<WebClient> stream() {
        return REGISTRY.stream();
    }
}
