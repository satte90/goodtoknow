package com.teliacompany.tiberius.base.server.secrets.agent.provider;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public final class SecretsProviderType {
    private final Type type;
    private final String value;

    /**
     * Instantiate via parse or static initializers
     */
    private SecretsProviderType(Type type, String value) {
        this.type = type;
        this.value = value;
    }

    public static SecretsProviderType parse(String secretsProvider) {
        if(StringUtils.isBlank(secretsProvider)) {
            return new SecretsProviderType(Type.NONE, secretsProvider);
        }
        var type = Arrays.stream(SecretsProviderType.Type.values())
                .filter(v -> v.name().equalsIgnoreCase(secretsProvider))
                .findFirst()
                .orElse(Type.CUSTOM);

        return new SecretsProviderType(type, secretsProvider);
    }

    public static SecretsProviderType tiberiusVault() {
        return new SecretsProviderType(Type.TIBERIUS_VAULT, "tiberius_vault");
    }

    public String getValue() {
        return value;
    }

    public boolean isLocal() {
        return type.equals(Type.LOCAL);
    }

    public boolean isAws() {
        return type.equals(Type.AWS_SECRETS_MANAGER);
    }

    public boolean isCustom() {
        return type.equals(Type.CUSTOM);
    }

    public boolean isTiberiusVault() {
        return type.equals(Type.TIBERIUS_VAULT);
    }

    public boolean isNone() {
        return type.equals(Type.NONE);
    }

    private enum Type {
        TIBERIUS_VAULT, AWS_SECRETS_MANAGER, LOCAL, CUSTOM, NONE;
    }
}
