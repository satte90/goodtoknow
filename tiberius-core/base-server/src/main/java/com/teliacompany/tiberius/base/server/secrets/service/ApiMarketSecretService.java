package com.teliacompany.tiberius.base.server.secrets.service;

import com.teliacompany.apimarket4j.core.ApiMarketOAuth2Service;
import com.teliacompany.apimarket4j.core.config.ApiMarketConnectionConfig;
import com.teliacompany.tiberius.base.server.api.BaseErrors;
import com.teliacompany.tiberius.base.server.config.SecretConfig;
import com.teliacompany.tiberius.base.server.secrets.agent.provider.SecretsProviderType;
import com.teliacompany.webflux.error.exception.server.InternalServerErrorException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@ConditionalOnClass(ApiMarketOAuth2Service.class)
@Service
public class ApiMarketSecretService implements SecretService {
    public static final String API_MARKET_SECRET_NAME = "apimarket";
    public static final String API_MARKET_ID = "apimarket_id";

    private final SecretsProviderType secretsProviderType;
    private final ApiMarketOAuth2Service apiMarketOAuth2Service;
    private final ApiMarketConnectionConfig apiMarketConnectionConfig;

    public ApiMarketSecretService(SecretConfig secretConfig, ApiMarketOAuth2Service apiMarketOAuth2Service, ApiMarketConnectionConfig apiMarketConnectionConfig) {
        this.apiMarketOAuth2Service = apiMarketOAuth2Service;
        this.apiMarketConnectionConfig = apiMarketConnectionConfig;
        this.secretsProviderType = secretConfig.getSecretsProviderType();
    }

    @Override
    public List<String> requestSecretNames() {
        return List.of(API_MARKET_SECRET_NAME, API_MARKET_ID);
    }

    @Override
    public void onSecretsReceived(Map<String, String> secrets) {
        String apiMarketSecret = secrets.get(API_MARKET_SECRET_NAME);
        String apiMarketId = secrets.get(API_MARKET_ID);
        if(apiMarketId != null) {
            this.apiMarketConnectionConfig.key = apiMarketId;
        }
        if(apiMarketSecret != null) {
            this.apiMarketConnectionConfig.secret = apiMarketSecret;
            this.apiMarketOAuth2Service.updateToken();
        } else if(secretsProviderType.isLocal()) {
            throw new InternalServerErrorException(BaseErrors.MISSING_ENVIRONMENT_VARIABLE, "Missing environment variable \"{}\", add it to your local system environment variables!", API_MARKET_SECRET_NAME);
        }
    }
}
