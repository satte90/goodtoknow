package com.teliacompany.tiberius.base.toca.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@EnableCaching
@Configuration
@ComponentScan(basePackages = "com.teliacompany.tiberius.base.toca")
@Import(TocaCorsConfig.class)
public class TiberiusTocaAutoConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(TiberiusTocaAutoConfiguration.class);

    public TiberiusTocaAutoConfiguration() {
        LOG.info("********** Tiberius Toca Enabled **********");
    }

}
