package com.teliacompany.apigee4j.core;

import com.teliacompany.apigee4j.core.config.ApigeeConnectionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.Base64Utils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.Locale;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ApigeeOAuth2Service {
    private static final Logger LOG = LoggerFactory.getLogger(ApigeeOAuth2Service.class);

    private final ApigeeConnectionConfig apigeeConnectionConfig;

    private WebClient webClient;
    private OAuth2Response cachedResponse;

    public ApigeeOAuth2Service(ApigeeConnectionConfig apigeeConnectionConfig, ApigeeProxyHelper apigeeProxyHelper) {
        this.apigeeConnectionConfig = apigeeConnectionConfig;

        WebClient.Builder builder = WebClient.builder();

        if (apigeeProxyHelper.isProxyActive()) {
            LOG.info("ApigeeOAuth2Service loaded with proxy enabled");
            builder.clientConnector(apigeeProxyHelper.getReactorClientHttpConnector());
        }

        this.webClient = builder.build();

        this.setOauthBaseUrl(apigeeConnectionConfig.getRefreshUrl());
    }

    @PostConstruct
    public void init() {
        LOG.info("ApigeeOAuth2Service loaded with token refresh url: {}", apigeeConnectionConfig.getRefreshUrl());
    }

    public void setApigeeUrl(String url) {
        final String newUrl = url + apigeeConnectionConfig.refreshEndpoint;
        setOauthBaseUrl(newUrl);
        LOG.info("ApigeeOAuth2ExchangeFilter token refresh url set to: {}", newUrl);
    }

    public void resetApigeeUrl() {
        final String url = apigeeConnectionConfig.getRefreshUrl();
        setOauthBaseUrl(url);
        LOG.info("ApigeeOAuth2ExchangeFilter token refresh url reset to: {}", url);
    }

    @Scheduled(fixedDelay = 1000 * 60 * 10, initialDelay = 0)
    public void updateToken() {
        LOG.debug("Timer refreshing access token");
        synchronizeRefreshToken()
                .subscribe(response -> {
                    cachedResponse = response;
                    LOG.debug("Access token updated! Expires {} ", response.getExpires_in());
                });
    }

    Mono<OAuth2Response> getOAuthResponse() {
        return getOAuthResponse(false);
    }

    Mono<OAuth2Response> getOAuthResponse(boolean forceUpdate) {
        if(cachedResponse == null || cachedResponse.hasExpired() || forceUpdate) {
            LOG.info("Refreshing apigee access token");
            return synchronizeRefreshToken()
                    .map(r -> {
                        this.cachedResponse = r;
                        LOG.info("Access token refreshed");
                        return r;
                    });
        }
        return Mono.just(cachedResponse);
    }

    private Mono<OAuth2Response> synchronizeRefreshToken() {
        return requestRefreshToken()
                .map(body -> {
                    Objects.requireNonNull(body).calcExpiresTime();
                    LOG.debug("Get a new OAuth2-object='{}'", body);
                    return body;
                });
    }

    void setOauthBaseUrl(String oauthBaseUrl) {
        webClient = webClient.mutate()
                .baseUrl(String.format(Locale.ROOT, "%s?grant_type=client_credentials", oauthBaseUrl))
                .build();
    }

    private Mono<OAuth2Response> requestRefreshToken() {
        final String basicAuth = Base64Utils.encodeToString((apigeeConnectionConfig.key + ":" + apigeeConnectionConfig.secret).getBytes(UTF_8));
       LOG.error("-------------> "+ "Basic " + basicAuth);
        WebClient.RequestBodySpec requestBodySpec = webClient.post()
                .header("Authorization", "Basic " + basicAuth);
        return requestBodySpec
                .retrieve()
                .onStatus(ApigeeOAuth2Service::isNotSuccess, clientResponse -> Mono.error(new ApigeeAuthenticationException("Could not refresh access token. Got status " + clientResponse.statusCode().value() + ": " + clientResponse.statusCode().name())))
                .bodyToMono(OAuth2Response.class);
    }

    private static boolean isNotSuccess(HttpStatus httpStatus) {
        return !httpStatus.is2xxSuccessful();
    }

    void clearToken() {
        cachedResponse = null;
    }

    void invalidateToken() {
        cachedResponse.setAccess_token("invalid");
    }
}
