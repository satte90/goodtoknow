package com.teliacompany.apigee4j.core.config;

import com.teliacompany.apigee4j.core.ApigeeOAuth2ExchangeFilter;
import com.teliacompany.apigee4j.core.ApigeeOAuth2Service;
import com.teliacompany.apigee4j.core.ApigeeProxyHelper;
import com.teliacompany.apigee4j.core.client.AbstractApigeeClient;
import com.teliacompany.apigee4j.core.client.ApigeeClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;


@Configuration
@Import(ApigeeConnectionConfig.class)
@ComponentScan(
        basePackages = {"com.teliacompany.apigee4j"},
        includeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = ApigeeClientConfig.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = AbstractApigeeClient.class)
        })
public class ApigeeWebClientAutoConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(ApigeeWebClientAutoConfiguration.class);

    @Bean
    public ApigeeOAuth2Service apigeeOAuth2Service(ApigeeConnectionConfig apigeeConnectionConfig, ApigeeProxyHelper apigeeProxyHelper) {
        LOG.info("Creating bean ApigeeOAuth2Service");
        return new ApigeeOAuth2Service(apigeeConnectionConfig, apigeeProxyHelper);
    }

    @Bean
    public ApigeeOAuth2ExchangeFilter apigeeOAuth2ExchangeFilter(ApigeeOAuth2Service apigeeOAuth2Service) {
        LOG.info("Creating bean apigeeOAuth2ExchangeFilter");
        return new ApigeeOAuth2ExchangeFilter(apigeeOAuth2Service);
    }
}
