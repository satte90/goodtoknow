package com.teliacompany.tiberius.base.server.testsupport.service;

import com.teliacompany.tiberius.base.server.api.TestModeData;
import com.teliacompany.tiberius.base.server.config.ApplicationProperties;
import com.teliacompany.tiberius.base.server.service.CurrentTimeProvider;
import com.teliacompany.tiberius.base.server.testsupport.testmode.listener.TiberiusTestModeEventListener;
import io.swagger.v3.oas.annotations.Hidden;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.List;

@Service
@Profile({"componenttest", "local"})
@Hidden
public class TiberiusTestSupportService {
    private static final Logger LOG = LoggerFactory.getLogger(TiberiusTestSupportService.class);
    private static final Logger TEST_LOG = LoggerFactory.getLogger("com.teliacompany.tiberius.startup.Logger");

    private final CurrentTimeProvider currentTimeProvider;
    private final List<TiberiusTestModeEventListener> testModeEventListeners;
    private final ApplicationProperties appConfig;

    @Autowired
    public TiberiusTestSupportService(ApplicationProperties appConfig, CurrentTimeProvider currentTimeProvider, List<TiberiusTestModeEventListener> testModeEventListeners) {
        this.appConfig = appConfig;
        this.currentTimeProvider = currentTimeProvider;
        this.testModeEventListeners = testModeEventListeners;
        this.testModeEventListeners.forEach(listener -> LOG.info("Registered test mode listener: {}", listener.getClass().getSimpleName()));
    }

    @PostConstruct
    public void init() {
        if(appConfig.getWiremockPort() != null) {
            final String globalBaseUriOverride = "http://localhost:" + appConfig.getWiremockPort();
            appConfig.setBaseUriOverride(globalBaseUriOverride);
        }
    }

    public Mono<Void> enableTestMode(TestModeData testModeData) {
        TEST_LOG.info("\n\n🚧\033[0;33m Enabling Test Mode\033[1m...\033[0m");

        testModeEventListeners.forEach(listener -> listener.enableTestMode(testModeData));

        TEST_LOG.info("🚧\033[0;33m Enabling Test Mode: \033[1mDone\033[0m\n\n");

        return Mono.empty();
    }

    public Mono<Void> disableTestMode() {
        TEST_LOG.info("\n\n🚧\033[0;33m Disabling Test Mode\033[1m...\033[0m");

        testModeEventListeners.forEach(TiberiusTestModeEventListener::disableTestMode);

        TEST_LOG.info("🚧\033[0;33m Disabling Test Mode: \033[1mDone\033[0m\n\n");
        return Mono.empty();
    }

    public Mono<Void> logTestName(String testName) {
        TEST_LOG.info("\n\n🚧\033[0;33m Running Test: \033[1m{}\033[0m\n\n", testName);
        return Mono.empty();
    }

    public Mono<Void> setTime(Instant instant) {
        currentTimeProvider.setClock(instant);
        return Mono.empty();
    }
}
