package com.teliacompany.tiberius.base.server.testsupport.testmode.listener;

import com.teliacompany.apimarket4j.core.ApiMarketOAuth2Service;
import com.teliacompany.webflux.request.client.WebClient;
import com.teliacompany.webflux.request.client.WebClientRegistry;
import com.teliacompany.tiberius.base.server.api.TestModeData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile({"componenttest", "local"})
@ConditionalOnClass({ApiMarketOAuth2Service.class})
public class ApimarketClientTestModeListener implements TiberiusTestModeEventListener {
    private static final Logger LOG = LoggerFactory.getLogger(ApimarketClientTestModeListener.class);

    private final ApiMarketOAuth2Service apiMarketOAuth2Service;

    public ApimarketClientTestModeListener(ApiMarketOAuth2Service apiMarketOAuth2Service) {
        LOG.info("Created Apimarket4JClientTestModeListener");
        this.apiMarketOAuth2Service = apiMarketOAuth2Service;
    }

    @Override
    public void enableTestMode(TestModeData testModeData) {
        final String hostOverride = "http://localhost:" + testModeData.getWiremockPort();
        apiMarketOAuth2Service.setApiMarketUrl(hostOverride);
    }

    @Override
    public void disableTestMode() {
        apiMarketOAuth2Service.resetApiMarketUrl();

    }
}
