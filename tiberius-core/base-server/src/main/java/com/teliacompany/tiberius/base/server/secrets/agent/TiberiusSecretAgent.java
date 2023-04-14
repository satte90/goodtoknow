package com.teliacompany.tiberius.base.server.secrets.agent;

import com.teliacompany.tiberius.base.server.integration.slack.SlackPanicClient;
import com.teliacompany.tiberius.base.server.secrets.service.SecretService;
import com.teliacompany.webflux.error.exception.server.InternalServerErrorException;
import com.teliacompany.tiberius.base.server.auth.key.PublicKeyProvider;
import com.teliacompany.tiberius.base.server.integration.auth.TiberiusUserAuthClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.lang.Nullable;
import reactor.core.publisher.Mono;

import java.security.PublicKey;
import java.util.List;

public class TiberiusSecretAgent implements SecretAgent {
    private static final Logger LOG = LoggerFactory.getLogger(TiberiusSecretAgent.class);
    private final ConfigurableEnvironment environment;
    private final PublicKeyProvider publicKeyProvider;
    private final TiberiusUserAuthClient tiberiusUserAuthClient;
    private final List<SecretService> tiberiusSecretServices;
    private final String appName;

    public TiberiusSecretAgent(ConfigurableEnvironment environment,
                               List<PublicKeyProvider> publicKeyProviders,
                               List<SecretService> tiberiusSecretServices,
                               TiberiusUserAuthClient tiberiusUserAuthClient,
                               @Value("${spring.application.name:unknown}") String appName) {
        this.environment = environment;
        this.publicKeyProvider = PublicKeyProvider.getPrioritizedKeyProvider(publicKeyProviders);
        this.tiberiusUserAuthClient = tiberiusUserAuthClient;
        this.tiberiusSecretServices = tiberiusSecretServices;
        this.appName = appName;
    }

    @Override
    public Mono<Boolean> fetchSecrets(@Nullable PublicKey key) {
        LOG.info("Tiberius Vault: ENABLED");
        if(key == null) {
            final String message = "No key to fetch secrets exist! Cannot fetch secrets from tiberius vault";
            LOG.error(message);
            SlackPanicClient.postSlackPanicMessage(environment, appName, message);
            return Mono.just(false);
        }
        SecretAgentHelper helper = new SecretAgentHelper(this.tiberiusSecretServices, key);

        LOG.info("Fetching {} secrets...", helper.getSecretsRequest().size());
        return tiberiusUserAuthClient.getSecrets(helper.getSecretsRequest())
                .map(helper::updateSecretsAndNotifySecretServices)
                .thenReturn(true);
    }

    @Override
    public Mono<Boolean> refreshSecrets() {
        if(publicKeyProvider == null) {
            throw new InternalServerErrorException("Cannot refresh secrets, no public key provider found");
        }
        return publicKeyProvider.getLatestPublicKey()
                .flatMap(this::fetchSecrets);
    }
}
