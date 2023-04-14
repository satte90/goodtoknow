package com.teliacompany.tiberius.base.server.event;

import com.teliacompany.tiberius.base.server.service.DevOpsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;

@Configuration(value = "TiberiusShutdown")
public class TiberiusShutdownEvent implements ApplicationListener<ContextClosedEvent> {
    private static final Logger STARTUP_LOG = LoggerFactory.getLogger("com.teliacompany.tiberius.startup.Logger");
    private boolean applicationClosedEventRegistered = false;

    private final DevOpsService devOpsService;

    public TiberiusShutdownEvent(DevOpsService devOpsService) {
        this.devOpsService = devOpsService;
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent contextClosedEvent) {
        if(!applicationClosedEventRegistered) {
            applicationClosedEventRegistered = true;
            STARTUP_LOG.info("\n\033[0;33mApplication Shutting down...\033[0m");
            devOpsService.onShutdown();
        }
    }
}
