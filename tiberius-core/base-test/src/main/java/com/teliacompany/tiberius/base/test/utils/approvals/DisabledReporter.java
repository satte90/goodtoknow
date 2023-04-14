package com.teliacompany.tiberius.base.test.utils.approvals;

import org.approvaltests.reporters.EnvironmentAwareReporter;

public class DisabledReporter implements EnvironmentAwareReporter {

    @Override
    public void report(String received, String approved) {
        throw new RuntimeException("Reporter is disabled");
    }

    @Override
    public boolean isWorkingInThisEnvironment(String forFile) {
        return false;
    }
}
