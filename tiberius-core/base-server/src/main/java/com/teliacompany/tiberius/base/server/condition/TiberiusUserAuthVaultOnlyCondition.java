package com.teliacompany.tiberius.base.server.condition;

import com.teliacompany.tiberius.base.server.secrets.agent.provider.SecretsProviderType;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import static com.teliacompany.tiberius.base.server.condition.TiberiusUserAuthEnabledCondition.isTiberiusAuthDisabled;

public class TiberiusUserAuthVaultOnlyCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        // If tiberius.vault is enabled and tiberius.user.auth is disabled, then the condition TiberiusUserAuthVaultCondition = true
        // Results in an extended TiberiusDisabledAuthenticationManager that fetches public keys on startup which are needed for secrets
        return isTiberiusAuthDisabled(context) && isTiberiusVaultEnabled(context);
    }

    static boolean isTiberiusVaultDisabled(ConditionContext context) {
        return !isTiberiusVaultEnabled(context);
    }

    static boolean isTiberiusVaultEnabled(ConditionContext context) {
        //Default false
        Boolean tiberiusVaultEnabledProperty = context.getEnvironment().getProperty("tiberius.vault.enabled", Boolean.class, false);
        String secretsProvider = context.getEnvironment().getProperty("tiberius.secrets.provider", String.class, "local");
        SecretsProviderType secretsProviderType = SecretsProviderType.parse(secretsProvider);
        return secretsProviderType.isTiberiusVault() || tiberiusVaultEnabledProperty.equals(true);
    }
}
