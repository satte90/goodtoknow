package com.teliacompany.tiberius.base.server.secrets.agent.provider;

import com.teliacompany.tiberius.base.server.auth.key.PublicKeyProvider;
import com.teliacompany.tiberius.base.server.integration.auth.TiberiusUserAuthClient;
import com.teliacompany.tiberius.base.server.integration.aws.TiberiusAwsSecretsManagerWebClient;
import com.teliacompany.tiberius.base.server.secrets.agent.AwsSecretAgent;
import com.teliacompany.tiberius.base.server.secrets.agent.DisabledSecretAgent;
import com.teliacompany.tiberius.base.server.secrets.agent.LocalSecretAgent;
import com.teliacompany.tiberius.base.server.secrets.agent.SecretAgent;
import com.teliacompany.tiberius.base.server.secrets.service.SecretService;
import com.teliacompany.tiberius.base.server.secrets.agent.TiberiusSecretAgent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DefaultSecretAgentProvider implements SecretAgentProvider {
    private final List<PublicKeyProvider> publicKeyProviders;
    private final List<SecretService> tiberiusSecretServices;
    private final TiberiusUserAuthClient tiberiusUserAuthClient;
    private final TiberiusAwsSecretsManagerWebClient awsSecretsManagerWebClient;
    private final ConfigurableEnvironment env;
    private final String appName;

    public DefaultSecretAgentProvider(List<PublicKeyProvider> publicKeyProviders,
                                      List<SecretService> tiberiusSecretServices,
                                      TiberiusUserAuthClient tiberiusUserAuthClient,
                                      TiberiusAwsSecretsManagerWebClient awsSecretsManagerWebClient,
                                      ConfigurableEnvironment env,
                                      @Value("${spring.application.name:unknown}") String appName) {
        this.publicKeyProviders = publicKeyProviders;
        this.tiberiusSecretServices = tiberiusSecretServices;
        this.tiberiusUserAuthClient = tiberiusUserAuthClient;
        this.awsSecretsManagerWebClient = awsSecretsManagerWebClient;
        this.env = env;
        this.appName = appName;
    }

    /**
     * Indicates that this is the default secret agent provider
     *
     * @return true
     */
    @Override
    public boolean isDefault() {
        return true;
    }

    @Override
    public SecretAgent provideSecretAgent(SecretsProviderType secretsProviderType) {
        if(secretsProviderType.isTiberiusVault()) {
            return new TiberiusSecretAgent(env, publicKeyProviders, tiberiusSecretServices, tiberiusUserAuthClient, appName);
        }
        if(secretsProviderType.isAws()) {
            return new AwsSecretAgent(tiberiusSecretServices, awsSecretsManagerWebClient);
        }
        if(secretsProviderType.isLocal()) {
            return new LocalSecretAgent(tiberiusSecretServices);
        }
        return new DisabledSecretAgent();
    }
}
