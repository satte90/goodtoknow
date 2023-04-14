package com.teliacompany.tiberius.base.server.testsupport.testmode.listener;

import com.teliacompany.spock4j.core.SpockAuthService;
import com.teliacompany.tiberius.base.server.api.TestModeData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Profile({"componenttest", "local"})
@ConditionalOnClass({SpockAuthService.class})
public class SpockClientTestModeListener implements TiberiusTestModeEventListener {
    private static final Logger LOG = LoggerFactory.getLogger(SpockClientTestModeListener.class);

    private final List<SpockAuthService> spockAuthServices;

    public SpockClientTestModeListener(List<SpockAuthService> spockAuthServices) {
        LOG.info("Created Spock4jTestModeListener");
        this.spockAuthServices = spockAuthServices;
    }

    @Override
    public void enableTestMode(TestModeData testModeData) {
        final String hostOverride = "http://localhost:" + testModeData.getWiremockPort();
        spockAuthServices.forEach(authService -> authService.setAuthHost(hostOverride));
    }

    @Override
    public void disableTestMode() {
        spockAuthServices.forEach(SpockAuthService::resetAuthHost);
    }
}
