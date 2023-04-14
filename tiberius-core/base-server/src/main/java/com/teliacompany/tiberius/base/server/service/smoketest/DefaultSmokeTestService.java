package com.teliacompany.tiberius.base.server.service.smoketest;

import java.util.ArrayList;
import java.util.List;


public class DefaultSmokeTestService implements SmokeTestService {
    @Override
    public List<SmokeTest<?>> getTests() {
        return new ArrayList<>();
    }
}
