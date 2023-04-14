package com.teliacompany.tiberius.base.server.integration.auth;

import com.teliacompany.webflux.request.client.WebClient;
import com.teliacompany.webflux.request.client.WebClientBuilder;
import com.teliacompany.webflux.request.client.WebClientConfig;
import com.teliacompany.tiberius.base.utils.BodyExtractorUtils;
import com.teliacompany.tiberius.crypto.CryptoUtils;
import com.teliacompany.tiberius.user.auth.api.v1.Base64Key;
import com.teliacompany.tiberius.user.auth.api.v1.Endpoints;
import com.teliacompany.tiberius.user.auth.api.v1.Endpoints.Secrets;
import com.teliacompany.tiberius.user.auth.api.v1.secrets.SecretResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Service
public class TiberiusUserAuthClient {
    private static final Logger LOG = LoggerFactory.getLogger(TiberiusUserAuthClient.class);
    private final WebClient client;
    private final String tiberiusApiKey;
    private final String tiberiusApiSecret;

    public TiberiusUserAuthClient(@Value("${tiberius.user.auth.host}") String host,
                                  @Value("${tiberius.user.auth.base.path}") String userAuthBasePath,
                                  @Value("${tiberius.api.key}") String tiberiusApiKey,
                                  @Value("${tiberius.api.secret}") String tiberiusApiSecret) {
        this.tiberiusApiKey = tiberiusApiKey;
        this.tiberiusApiSecret = tiberiusApiSecret;
        WebClientConfig config = WebClientConfig.builder()
                .withServiceName("Tiberius User Auth")
                .withHost(host)
                .withBasePath(userAuthBasePath)
                .build();
        this.client = WebClientBuilder.withConfig(config).build();
    }

    public Mono<Base64Key> getLatestKey() {
        final String oneTimeToken = CryptoUtils.encrypt(tiberiusApiKey, tiberiusApiSecret);
        return client.get(Endpoints.SIGNING_KEYS_LATEST)
                .header("Authorization", "Bearer " + oneTimeToken)
                .retrieve(Base64Key.class)
                .map(BodyExtractorUtils.extractBody(client.getServiceName()));
    }

    public Mono<Base64Key> getKey(String id) {
        final String oneTimeToken = CryptoUtils.encrypt(tiberiusApiKey, tiberiusApiSecret);
        return client.get(Endpoints.SIGNING_KEYS + "/{id}")
                .uriVariable("id", id)
                .header("Authorization", "Bearer " + oneTimeToken)
                .retrieve(Base64Key.class)
                .doOnError(e -> LOG.error("Could not get signing key", e))
                .map(BodyExtractorUtils.extractBody(client.getServiceName()));
    }

    public Mono<List<SecretResponse>> getSecrets(Collection<String> secretNames) {
        final String secretNamesCsv = StringUtils.join(secretNames, ",");
        final String oneTimeToken = CryptoUtils.encrypt(tiberiusApiKey, tiberiusApiSecret);
        return client.get(Secrets.BASE_PATH + "/" + Secrets.ENCRYPTED)
                .uriVariable("names", secretNamesCsv)
                .header("Authorization", "Bearer " + oneTimeToken)
                .retrieve(SecretResponse[].class)
                .map(BodyExtractorUtils.extractBody(client.getServiceName()))
                .map(Arrays::asList)
                .onErrorResume(e -> {
                    LOG.error("Could not get secrets", e);
                    return Mono.just(Collections.emptyList());
                });
    }
}
