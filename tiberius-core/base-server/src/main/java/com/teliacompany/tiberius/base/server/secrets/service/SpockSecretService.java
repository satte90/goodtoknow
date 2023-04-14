package com.teliacompany.tiberius.base.server.secrets.service;

import com.teliacompany.spock4j.core.SpockAuthService;
import com.teliacompany.spock4j.core.config.connection.SpockApiMarketConfig;
import com.teliacompany.spock4j.core.config.connection.SpockDirectConnectConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@ConditionalOnClass({SpockAuthService.class})
public class SpockSecretService implements SecretService {
    private static final Logger LOG = LoggerFactory.getLogger(ApigeeSecretService.class);
    public static final String SPOCK_USER = "spock_user";
    public static final String SPOCK_SECRET = "spock_secret";
    public static final String TOCA_API_MARKET_ID = "toca_apimarket_id";
    public static final String TOCA_API_MARKET_SECRET = "toca_apimarket_secret";

    private final List<SpockAuthService> spockAuthServices;
    private final SpockDirectConnectConfig spockDirectConnectConfig;
    private final SpockApiMarketConfig spockApiMarketConfig;

    public SpockSecretService(List<SpockAuthService> spockAuthServices, SpockDirectConnectConfig spockDirectConnectConfig, SpockApiMarketConfig spockApiMarketConfig) {
        this.spockAuthServices = spockAuthServices;
        this.spockDirectConnectConfig = spockDirectConnectConfig;
        this.spockApiMarketConfig = spockApiMarketConfig;
    }

    @Override
    public List<String> requestSecretNames() {
        return List.of(SPOCK_USER, SPOCK_SECRET, TOCA_API_MARKET_ID, TOCA_API_MARKET_SECRET);
    }

    @Override
    public void onSecretsReceived(Map<String, String> secrets) {
        String spockSecret = secrets.get(SPOCK_SECRET);
        String spockId = secrets.get(SPOCK_USER);
        if(spockId != null) {
            LOG.info("Got secret for {}", SPOCK_USER);
            this.spockDirectConnectConfig.setUser(spockId);
        }
        if(spockSecret != null) {
            LOG.info("Got secret for {}", SPOCK_SECRET);
            this.spockDirectConnectConfig.setPassword(spockSecret);
        }

        String tocaApiMarketId = secrets.get(TOCA_API_MARKET_ID);
        String tocaApiMarketSecret = secrets.get(TOCA_API_MARKET_SECRET);
        if(tocaApiMarketId != null) {
            LOG.info("Got secret for {}", TOCA_API_MARKET_ID);
            this.spockApiMarketConfig.setUser(tocaApiMarketId);
        }
        if(tocaApiMarketSecret != null) {
            LOG.info("Got secret for {}", TOCA_API_MARKET_SECRET);
            this.spockApiMarketConfig.setPassword(tocaApiMarketSecret);
        }

        this.spockAuthServices.forEach(SpockAuthService::updateToken);
    }
}
