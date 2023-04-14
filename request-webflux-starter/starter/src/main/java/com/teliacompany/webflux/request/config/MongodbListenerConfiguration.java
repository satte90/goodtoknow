package com.teliacompany.webflux.request.config;

import com.teliacompany.webflux.request.mongodb.MongoDbEventListener;
import com.teliacompany.webflux.request.log.RequestLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;

@ConditionalOnClass(AbstractMongoEventListener.class)
@Configuration
public class MongodbListenerConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(MongodbListenerConfiguration.class);

    @Bean
    public MongoDbEventListener mongoDbEventListener(RequestLogger requestLogger) {
        LOG.info("Making mongoDbEventListener...");
        return new MongoDbEventListener(requestLogger);
    }
}
