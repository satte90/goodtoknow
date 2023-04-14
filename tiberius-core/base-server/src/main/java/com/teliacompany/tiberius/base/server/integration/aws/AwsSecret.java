package com.teliacompany.tiberius.base.server.integration.aws;

public class AwsSecret {
    private String name;
    private String value;

    public AwsSecret(String secretName, String secret) {
        this.name = secretName;
        this.value = secret;
    }

    public String getName() {
        return name;
    }

    public AwsSecret setName(String name) {
        this.name = name;
        return this;
    }

    public String getValue() {
        return value;
    }

    public AwsSecret setValue(String value) {
        this.value = value;
        return this;
    }
}
