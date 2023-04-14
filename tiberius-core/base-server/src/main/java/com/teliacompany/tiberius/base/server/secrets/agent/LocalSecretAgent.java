package com.teliacompany.tiberius.base.server.secrets.agent;

import com.teliacompany.tiberius.base.server.secrets.service.SecretService;
import com.teliacompany.tiberius.user.auth.api.v1.secrets.SecretResponse;
import com.teliacompany.webflux.error.exception.server.InternalServerErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.PublicKey;
import java.util.List;

public class LocalSecretAgent implements SecretAgent {
    private static final Logger LOG = LoggerFactory.getLogger(LocalSecretAgent.class);
    public static final String ENV_SECRET_CRYPTO_KEY = "n0t_very_SECRET";

    private final List<SecretService> tiberiusSecretServices;

    public LocalSecretAgent(List<SecretService> tiberiusSecretServices) {
        this.tiberiusSecretServices = tiberiusSecretServices;
    }

    @Override
    public Mono<Boolean> fetchSecrets(@Nullable PublicKey notUsed) {
        LOG.info("Local Env Secrets: ENABLED");
        SecretAgentHelper helper = new SecretAgentHelper(this.tiberiusSecretServices, ENV_SECRET_CRYPTO_KEY);

        return Flux.fromIterable(helper.getSecretsRequest())
                .map(this::getSecretFromEnvironmentVariables)
                .collectList()
                .map(helper::updateSecretsAndNotifySecretServices)
                .thenReturn(true);
    }

    private SecretResponse getSecretFromEnvironmentVariables(String secretName) {
        final String encryptedSecret = System.getenv(secretName);
        if(encryptedSecret == null) {
            throw new InternalServerErrorException("Could not find secret environment variable " + secretName);
        }
        return new SecretResponse(secretName, encryptedSecret, null);
    }

    @Override
    public Mono<Boolean> refreshSecrets() {
        return this.fetchSecrets(null);
    }
}
