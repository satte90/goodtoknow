package com.teliacompany.tiberius.base.server.secrets.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.teliacompany.apigee4j.core.ApigeeOAuth2Service;
import com.teliacompany.apigee4j.core.config.ApigeeConnectionConfig;
import com.teliacompany.tiberius.base.server.api.BaseErrors;
import com.teliacompany.tiberius.base.server.config.SecretConfig;
import com.teliacompany.tiberius.base.server.integration.aws.AwsSecretValue;
import com.teliacompany.tiberius.base.server.secrets.agent.provider.SecretsProviderType;
import com.teliacompany.webflux.error.exception.server.InternalServerErrorException;
import com.teliacompany.webflux.jackson.TeliaObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@ConditionalOnClass(ApigeeOAuth2Service.class)
@Service
public class ApigeeSecretService implements SecretService {
    private static final Logger LOG = LoggerFactory.getLogger(ApigeeSecretService.class);
    public static final String APIGEE_SECRET_NAME = "apigee";
    public static final String APIGEE_ID = "apigee_id";
    public static final String AWS_APIGEE_NAME = "arn:aws:secretsmanager:eu-north-1:067718661431:secret:toca-dev/apigee-NCdZU8";

    private final SecretsProviderType secretsProviderType;
    private final ApigeeOAuth2Service apigeeOAuth2Service;
    private final ApigeeConnectionConfig apigeeConnectionConfig;

    public ApigeeSecretService(SecretConfig secretConfig, ApigeeOAuth2Service apigeeOAuth2Service, ApigeeConnectionConfig apigeeConnectionConfig) {
        this.apigeeOAuth2Service = apigeeOAuth2Service;
        this.apigeeConnectionConfig = apigeeConnectionConfig;
        this.secretsProviderType = secretConfig.getSecretsProviderType();
    }

    @Override
    public List<String> requestSecretNames() {
        if(secretsProviderType.isAws()) {
            return List.of(AWS_APIGEE_NAME);
        }
        return List.of(APIGEE_ID, APIGEE_SECRET_NAME);
    }

    @Override
    public void onSecretsReceived(Map<String, String> secrets) {
        if(secretsProviderType.isAws()) {
            String awsApigeeSecretJson = secrets.get(APIGEE_SECRET_NAME);
            AwsSecretValue secretValue = readAwsSecretValueFromJson(awsApigeeSecretJson);
            this.apigeeConnectionConfig.key = secretValue.getKey();
            this.apigeeConnectionConfig.secret = secretValue.getSecret();
            this.apigeeOAuth2Service.updateToken();
        } else {
            //For now the apigee key is not stored in tiberius vault, it should be a property.
            String apigeeSecret = secrets.get(APIGEE_SECRET_NAME);
            String apigeeId = secrets.get(APIGEE_ID);
            if(apigeeId != null) {
                LOG.info("Got secret for {}", APIGEE_ID);
                this.apigeeConnectionConfig.key = apigeeId;
            }
            if(apigeeSecret != null) {
                LOG.info("Got secret for {}", APIGEE_SECRET_NAME);
                this.apigeeConnectionConfig.secret = apigeeSecret;
                this.apigeeOAuth2Service.updateToken();
            } else if(secretsProviderType.isLocal()) {
                throw new InternalServerErrorException(BaseErrors.MISSING_ENVIRONMENT_VARIABLE, "Missing environment variable \"{}\", add it to your local system environment variables!", APIGEE_SECRET_NAME);
            }
        }
    }

    @NonNull
    private static AwsSecretValue readAwsSecretValueFromJson(String awsApigeeSecretJson) {
        try {
            return TeliaObjectMapper.get().readValue(awsApigeeSecretJson, AwsSecretValue.class);
        } catch(JsonProcessingException e) {
            LOG.error("Could not extract secret key and value from AwsSecret. Verify that the secret value is stored in json format containing key and secret");
        }
        return new AwsSecretValue()
                .setKey("NOT_SET")
                .setSecret("NOT_SET");
    }
}
