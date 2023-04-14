package com.teliacompany.tiberius.base.server.testsupport.testmode.listener;

import com.teliacompany.webflux.request.client.WebClient;
import com.teliacompany.webflux.request.client.WebClientRegistry;
import com.teliacompany.tiberius.base.server.api.TestModeData;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile({"componenttest", "local"})
public class WebClientTestModeListener implements TiberiusTestModeEventListener {
    @Override
    public void enableTestMode(TestModeData testModeData) {
        final String hostOverride = "http://localhost:" + testModeData.getWiremockPort();
        WebClientRegistry.stream().forEach(wc -> wc.setHost(hostOverride));
    }

    @Override
    public void disableTestMode() {
        WebClientRegistry.stream().forEach(WebClient::resetBaseUrl);
    }
}
