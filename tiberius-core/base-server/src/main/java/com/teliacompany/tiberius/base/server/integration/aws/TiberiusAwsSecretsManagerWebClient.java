package com.teliacompany.tiberius.base.server.integration.aws;

import com.teliacompany.tiberius.base.server.api.BaseErrors;
import com.teliacompany.tiberius.base.server.secrets.agent.provider.SecretsProviderType;
import com.teliacompany.tiberius.crypto.CryptoUtils;
import com.teliacompany.webflux.error.api.ErrorCause;
import com.teliacompany.webflux.error.exception.WebException;
import com.teliacompany.webflux.error.exception.server.InternalServerErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient.Builder;
import software.amazon.awssdk.http.nio.netty.ProxyConfiguration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerAsyncClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import static com.teliacompany.tiberius.base.server.integration.aws.TiberiusAwsSecretsManagerClientConfiguration.SERVICE_NAME;

@Component
public class TiberiusAwsSecretsManagerWebClient {
    private static final Logger LOG = LoggerFactory.getLogger(TiberiusAwsSecretsManagerWebClient.class);

    private AwsBasicCredentials awsBasicCredentials;
    private final SecretsManagerAsyncClient secretsManagerClient;

    public TiberiusAwsSecretsManagerWebClient(@Value("${tiberius.secrets.provider:local}") String secretsProvider, TiberiusAwsSecretsManagerClientConfiguration config) {
        ProxyConfiguration proxyConfig = ProxyConfiguration.builder()
                .host(config.getProxyHost())
                .port(config.getProxyPort())
                .scheme("http")
                .build();

        Builder clientBuilder = NettyNioAsyncHttpClient.builder();
        if(config.isProxyEnabled()) {
            LOG.info("Using proxy for aws secrets manager");
            clientBuilder.proxyConfiguration(proxyConfig);
        }
        SdkAsyncHttpClient client = clientBuilder.build();
        secretsManagerClient = SecretsManagerAsyncClient.builder()
                .httpClient(client)
                .region(Region.EU_NORTH_1)
                .credentialsProvider(() -> awsBasicCredentials)
                .build();

        if(config.getAccessKeyId() != null && config.getSecretAccessKey() != null) {
            if(config.isUseEncryptedEnvironmentVariables()) {
                String accessKeyId = CryptoUtils.decrypt(config.getAccessKeyId(), TiberiusAwsSecretsManagerClientConfiguration.AWS_CRYPTO_KEY);
                String secretAccessKey = CryptoUtils.decrypt(config.getSecretAccessKey(), TiberiusAwsSecretsManagerClientConfiguration.AWS_CRYPTO_KEY);
                setAwsBasicCredentials(accessKeyId, secretAccessKey);
            } else {
                setAwsBasicCredentials(config.getAccessKeyId(), config.getSecretAccessKey());
            }
        } else if(SecretsProviderType.parse(secretsProvider).isAws()) {
            throw new InternalServerErrorException(BaseErrors.STARTUP_ERROR, "Missing AWS Secrets Manager Credentials. Make sure aws.secretsmanager.accessKeyId and aws.secretsmanager.secretAccessKey is set. By default these are read from environment variables: AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY");
        }
    }

    /**
     * Creates new Aws credentials with provided keyId and secret that will be used by the secretsManagerClient.
     * This method can be used to dynamically set these values after fetching them from for example a vault.
     */
    public void setAwsBasicCredentials(String accessKeyId, String secretAccessKey) {
        this.awsBasicCredentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
    }

    public Mono<AwsSecret> getSecret(String secretName) {
        GetSecretValueRequest valueRequest = GetSecretValueRequest.builder()
                .secretId(secretName)
                .build();
        LOG.info("Requesting secret: {}", secretName);
        return Mono.fromCompletionStage(secretsManagerClient.getSecretValue(valueRequest))
                .onErrorMap(this::mapAwsError)
                .map(GetSecretValueResponse::secretString)
                .map(secret -> new AwsSecret(secretName, secret));
    }

    private Throwable mapAwsError(Throwable e) {
        LOG.error("Error fetching Aws Secrets", e);
        final ErrorCause cause = ErrorCause.from(SERVICE_NAME, e.getMessage(), e);
        AwsError error = AwsError.fromAwsException(e);
        return WebException.fromHttpStatus(error.getHttpStatus(), error, cause, "Error fetching aws secret");
    }
}
