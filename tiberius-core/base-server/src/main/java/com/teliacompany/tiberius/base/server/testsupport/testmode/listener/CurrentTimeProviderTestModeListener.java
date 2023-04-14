package com.teliacompany.tiberius.base.server.testsupport.testmode.listener;

import com.teliacompany.tiberius.base.server.api.TestModeData;
import com.teliacompany.tiberius.base.server.service.CurrentTimeProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile({"componenttest", "local"})
public class CurrentTimeProviderTestModeListener implements TiberiusTestModeEventListener {
    private final CurrentTimeProvider currentTimeProvider;

    public CurrentTimeProviderTestModeListener(CurrentTimeProvider currentTimeProvider) {
        this.currentTimeProvider = currentTimeProvider;
    }

    @Override
    public void enableTestMode(TestModeData testModeData) {
        if(testModeData.getTimestamp() != null) {
            currentTimeProvider.setClock(testModeData.getTimestamp());
        }
    }

    @Override
    public void disableTestMode() {
        currentTimeProvider.resetClock();
    }
}
