package com.teliacompany.tiberius.base.server.integration.aws;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TiberiusAwsSecretsManagerClientConfiguration {
    public static final String AWS_CRYPTO_KEY = "awsCryptoK3Y!"; //Do not use as security for production
    public static final String SERVICE_NAME = "AwsSecretsManager";
    private final boolean proxyEnabled;
    private final String proxyHost;
    private final Integer proxyPort;
    private final String accessKeyId;
    private final String secretAccessKey;
    private final boolean useEncryptedEnvironmentVariables;

    public TiberiusAwsSecretsManagerClientConfiguration(
            @Value("${aws.secretsmanager.useEncryptedKeys:false}") boolean useEncryptedEnvironmentVariables,
            @Value("${aws.secretsmanager.proxy.enabled}") boolean proxyEnabled,
            @Value("${aws.secretsmanager.proxy.host}") String proxyHost,
            @Value("${aws.secretsmanager.proxy.port}") Integer proxyPort,
            @Value("${aws.secretsmanager.accessKeyId}") String accessKeyId,
            @Value("${aws.secretsmanager.secretAccessKey}") String secretAccessKey) {
        this.useEncryptedEnvironmentVariables = useEncryptedEnvironmentVariables;
        this.proxyEnabled = proxyEnabled;
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.accessKeyId = accessKeyId;
        this.secretAccessKey = secretAccessKey;
    }

    public boolean isProxyEnabled() {
        return proxyEnabled;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public Integer getProxyPort() {
        return proxyPort;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public String getSecretAccessKey() {
        return secretAccessKey;
    }

    public boolean isUseEncryptedEnvironmentVariables() {
        return useEncryptedEnvironmentVariables;
    }
}
