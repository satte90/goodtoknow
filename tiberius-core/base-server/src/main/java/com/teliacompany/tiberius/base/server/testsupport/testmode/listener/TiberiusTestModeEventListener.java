package com.teliacompany.tiberius.base.server.testsupport.testmode.listener;

import com.teliacompany.tiberius.base.server.api.TestModeData;

/**
 * Implement to provide custom implementation to run whenever test mode is enabled nad disabled
 */
public interface TiberiusTestModeEventListener {
    void enableTestMode(TestModeData testModeData);

    void disableTestMode();
}
