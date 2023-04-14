package com.teliacompany.tiberius.base.server.config;

import com.teliacompany.tiberius.base.server.api.BaseErrors;
import com.teliacompany.tiberius.base.server.secrets.agent.SecretAgent;
import com.teliacompany.tiberius.base.server.secrets.agent.provider.DefaultSecretAgentProvider;
import com.teliacompany.tiberius.base.server.secrets.agent.provider.SecretAgentProvider;
import com.teliacompany.tiberius.base.server.secrets.agent.provider.SecretsProviderType;
import com.teliacompany.webflux.error.exception.server.InternalServerErrorException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class SecretConfig {
    private final SecretsProviderType secretsProviderType;

    public SecretConfig(@Value("${tiberius.vault.enabled:false}") boolean tiberiusVaultEnabled, @Value("${tiberius.secrets.provider:local}") String secretsProvider) {
        //Needed for backwards compatibility
        if(tiberiusVaultEnabled) {
            this.secretsProviderType = SecretsProviderType.tiberiusVault();
        } else {
            this.secretsProviderType = SecretsProviderType.parse(secretsProvider);
        }
    }

    public SecretsProviderType getSecretsProviderType() {
        return secretsProviderType;
    }

    @Bean
    public SecretAgent secretAgent(List<SecretAgentProvider> secretAgentProviders) {
        if(secretAgentProviders.size() > 2) {
            String providers = secretAgentProviders.stream().map(p -> p.getClass().getSimpleName()).collect(Collectors.joining(", "));
            throw new InternalServerErrorException(BaseErrors.STARTUP_ERROR, "To many secret agent providers. Only one provider allowed excluding {}. Found [{}]", DefaultSecretAgentProvider.class.getSimpleName(), providers);
        }

        return secretAgentProviders.stream()
                .max(Comparator.comparing(provider -> provider.isDefault() ? 0 : 1))
                .orElseThrow(() -> new InternalServerErrorException(BaseErrors.STARTUP_ERROR, "Could not find any secret agent providers. At least {} should be available. " +
                        "This is likely an error in Tiberius Core, have someone deleted the default provider?", DefaultSecretAgentProvider.class.getSimpleName()))
                .provideSecretAgent(this.secretsProviderType);
    }
}
