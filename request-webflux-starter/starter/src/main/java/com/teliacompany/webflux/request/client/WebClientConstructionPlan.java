package com.teliacompany.webflux.request.client;

import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;

public class WebClientConstructionPlan {
    private final Builder builder;
    private final ExchangeStrategies exchangeStrategies;
    private final ClientHttpConnector connector;

    public WebClientConstructionPlan(Builder builder, ExchangeStrategies exchangeStrategies, ClientHttpConnector connector) {
        this.builder = builder;
        this.exchangeStrategies = exchangeStrategies;
        this.connector = connector;
    }

    public WebClient build() {
        return builder.clientConnector(connector)
                .exchangeStrategies(exchangeStrategies)
                .build();
    }

    public ExchangeStrategies getExchangeStrategies() {
        return exchangeStrategies;
    }

    public ClientHttpConnector getConnector() {
        return connector;
    }

}
