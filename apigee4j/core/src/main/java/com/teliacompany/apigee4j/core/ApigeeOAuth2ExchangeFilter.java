package com.teliacompany.apigee4j.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

import java.util.function.Function;

public class ApigeeOAuth2ExchangeFilter implements ExchangeFilterFunction {
    private static final Logger LOG = LoggerFactory.getLogger(ApigeeOAuth2ExchangeFilter.class);


    private final ApigeeOAuth2Service apigeeOAuth2Service;

    public ApigeeOAuth2ExchangeFilter(ApigeeOAuth2Service apigeeOAuth2Service) {
        this.apigeeOAuth2Service = apigeeOAuth2Service;
    }

    @Override
    @NonNull
    public Mono<ClientResponse> filter(@NonNull ClientRequest request, @NonNull ExchangeFunction next) {
        return apigeeOAuth2Service.getOAuthResponse()
                .flatMap(oAuth2Response -> {
                    ClientRequest authorizedRequest = getAuthorizedRequest(request, oAuth2Response);

                    // Call & retry
                    return next.exchange(authorizedRequest)
                            .flatMap((Function<ClientResponse, Mono<ClientResponse>>) clientResponse -> {
                                // If status code is 401, refresh token and retry once.
                                if(clientResponse.statusCode().value() == 401) {
                                    return refreshTokenAndRetry(request, next, clientResponse);
                                } else {
                                    return Mono.just(clientResponse);
                                }
                            });
                });
    }

    private Mono<ClientResponse> refreshTokenAndRetry(ClientRequest request, ExchangeFunction next, ClientResponse clientResponse) {
        LOG.info("Unauthorized, force update access token and retry");
        return clientResponse.toBodilessEntity()
                .flatMap(v -> apigeeOAuth2Service.getOAuthResponse(true))
                .flatMap(oAuth2Response2 -> {
                    ClientRequest retryRequest = getAuthorizedRequest(request, oAuth2Response2);
                    return next.exchange(retryRequest);
                });
    }

    private static ClientRequest getAuthorizedRequest(@NonNull ClientRequest request, OAuth2Response oAuth2Response) {
        return ClientRequest.from(request)
                .header("Authorization", "Bearer " + oAuth2Response.getAccess_token())
                .build();
    }


}
