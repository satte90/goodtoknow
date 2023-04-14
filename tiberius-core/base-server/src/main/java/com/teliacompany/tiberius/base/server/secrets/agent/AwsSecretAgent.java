package com.teliacompany.tiberius.base.server.secrets.agent;

import com.teliacompany.tiberius.base.server.integration.aws.AwsSecret;
import com.teliacompany.tiberius.base.server.integration.aws.TiberiusAwsSecretsManagerWebClient;
import com.teliacompany.tiberius.base.server.secrets.service.SecretService;
import com.teliacompany.tiberius.user.auth.api.v1.secrets.SecretResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.PublicKey;
import java.util.List;

@SuppressWarnings("DuplicatedCode")
public class AwsSecretAgent implements SecretAgent {
    private static final Logger LOG = LoggerFactory.getLogger(AwsSecretAgent.class);
    private final List<SecretService> tiberiusSecretServices;
    private final TiberiusAwsSecretsManagerWebClient awsSecretsManagerWebClient;

    public AwsSecretAgent(List<SecretService> tiberiusSecretServices,
                          TiberiusAwsSecretsManagerWebClient awsSecretsManagerWebClient) {
        this.tiberiusSecretServices = tiberiusSecretServices;
        this.awsSecretsManagerWebClient = awsSecretsManagerWebClient;
    }

    @Override
    public Mono<Boolean> fetchSecrets(@Nullable PublicKey notUsed) {
        LOG.info("Aws Secrets: ENABLED");
        SecretAgentHelper helper = new SecretAgentHelper(this.tiberiusSecretServices);

        LOG.info("Fetching {} secrets...", helper.getSecretsRequest().size());
        return Flux.fromIterable(helper.getSecretsRequest())
                .flatMap(awsSecretsManagerWebClient::getSecret)
                .map(this::convertAwsSecret)
                .collectList()
                .map(helper::updateSecretsAndNotifySecretServices)
                .thenReturn(true);


    }

    private SecretResponse convertAwsSecret(AwsSecret awsSecret) {
        return new SecretResponse(awsSecret.getName(), awsSecret.getValue(), null);
    }

    @Override
    public Mono<Boolean> refreshSecrets() {
        return this.fetchSecrets(null);
    }
}
