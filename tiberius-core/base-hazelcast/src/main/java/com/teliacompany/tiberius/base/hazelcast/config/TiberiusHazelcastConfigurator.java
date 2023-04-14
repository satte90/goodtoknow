package com.teliacompany.tiberius.base.hazelcast.config;

import com.hazelcast.config.Config;

public interface TiberiusHazelcastConfigurator {
    void configure(Config config);
}
