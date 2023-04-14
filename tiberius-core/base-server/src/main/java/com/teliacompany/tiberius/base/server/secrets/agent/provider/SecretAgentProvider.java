package com.teliacompany.tiberius.base.server.secrets.agent.provider;

import com.teliacompany.tiberius.base.server.secrets.agent.SecretAgent;

/**
 * Implement this if you need a special secret agent provider to provide you with a special secret agent
 */
public interface SecretAgentProvider {
    SecretAgent provideSecretAgent(SecretsProviderType secretsProviderType);

    /**
     * Overridden by DefaultSecretAgentProvider to indicate it is the default provider if no other provider is present.
     * Do not override this method if the implementing class is not DefaultSecretAgentProvider
     * @return false
     */
    default boolean isDefault() {
        return false;
    }
}
