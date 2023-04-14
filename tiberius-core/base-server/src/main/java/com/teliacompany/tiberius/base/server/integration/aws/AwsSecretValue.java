package com.teliacompany.tiberius.base.server.integration.aws;

public class AwsSecretValue {
    private String key;
    private String secret;

    public String getKey() {
        return key;
    }

    public AwsSecretValue setKey(String key) {
        this.key = key;
        return this;
    }

    public String getSecret() {
        return secret;
    }

    public AwsSecretValue setSecret(String secret) {
        this.secret = secret;
        return this;
    }
}
