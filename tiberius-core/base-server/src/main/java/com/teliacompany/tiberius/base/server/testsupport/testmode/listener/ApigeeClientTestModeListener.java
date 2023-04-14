package com.teliacompany.tiberius.base.server.testsupport.testmode.listener;

import com.teliacompany.apigee4j.core.ApigeeOAuth2Service;
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
@ConditionalOnClass({ApigeeOAuth2Service.class})
public class ApigeeClientTestModeListener implements TiberiusTestModeEventListener {
    private static final Logger LOG = LoggerFactory.getLogger(ApigeeOAuth2Service.class);
    private final ApigeeOAuth2Service apigeeOAuth2Service;

    public ApigeeClientTestModeListener(ApigeeOAuth2Service apigeeOAuth2Service) {
        LOG.info("Created Apimarket4JClientTestModeListener");
        this.apigeeOAuth2Service = apigeeOAuth2Service;
    }

    @Override
    public void enableTestMode(TestModeData testModeData) {
        final String hostOverride = "http://localhost:" + testModeData.getWiremockPort();
        apigeeOAuth2Service.setApigeeUrl(hostOverride);
    }

    @Override
    public void disableTestMode() {
        apigeeOAuth2Service.resetApigeeUrl();

    }
}
