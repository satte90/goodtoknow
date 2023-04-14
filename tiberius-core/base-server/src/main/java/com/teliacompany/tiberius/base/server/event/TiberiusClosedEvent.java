package com.teliacompany.tiberius.base.server.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;

@Configuration(value = "TiberiusClosed")
public class TiberiusClosedEvent implements ApplicationListener<ContextClosedEvent> {
    private static final Logger STARTUP_LOG = LoggerFactory.getLogger("com.teliacompany.tiberius.startup.Logger");
    private boolean applicationClosedEventRegistered = false;

    @Override
    public void onApplicationEvent(ContextClosedEvent contextClosedEvent) {
        if(!applicationClosedEventRegistered) {
            applicationClosedEventRegistered = true;
            STARTUP_LOG.info("⚙️ \033[0;33m Application Closing...\033[0m");
        }
    }
}
