package com.teliacompany.webflux.request.config;

import com.teliacompany.webflux.request.client.WebClientInitializer;
import com.teliacompany.webflux.request.context.MdcContextLifter;
import com.teliacompany.webflux.request.metrics.DefaultMetricsReporter;
import com.teliacompany.webflux.request.metrics.DisabledMetricsReporter;
import com.teliacompany.webflux.request.metrics.MetricsReporter;
import com.teliacompany.webflux.request.processor.InternalRequestProcessor;
import com.teliacompany.webflux.request.processor.RequestProcessorImpl;
import com.teliacompany.webflux.request.processor.RestRequestProcessor;
import com.teliacompany.webflux.request.processor.error.ApplicationNameProvider;
import com.teliacompany.webflux.request.processor.error.ErrorAttributesProvider;
import com.teliacompany.webflux.request.processor.error.TraceLoggerProvider;
import com.teliacompany.webflux.request.status.PingController;
import com.teliacompany.webflux.request.RequestProcessor;
import com.teliacompany.webflux.request.log.DefaultRequestLogger;
import com.teliacompany.webflux.request.log.RequestLogger;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Operators;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;

@Configuration
@EnableConfigurationProperties(value = {LoggingConfig.class, MetricsConfig.class})
@ComponentScan(basePackageClasses = {PingController.class})
@Import(MongodbListenerConfiguration.class)
public class RequestWebfluxStarterAutoConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(RequestWebfluxStarterAutoConfiguration.class);
    private static final String MDC_CONTEXT_REACTOR_KEY = "TransactionContextMdcLifter";
    private final String applicationName;

    public RequestWebfluxStarterAutoConfiguration(@Value("${spring.application.name:unknown.spring.application.name}") String applicationName) {
        this.applicationName = applicationName;
    }

    @PostConstruct
    public void contextOperatorHook() {
        Hooks.onEachOperator(MDC_CONTEXT_REACTOR_KEY, Operators.lift((scannable, coreSubscriber) -> new MdcContextLifter<>(coreSubscriber)));
    }

    @PreDestroy
    public void cleanupHook() {
        Hooks.resetOnEachOperator(MDC_CONTEXT_REACTOR_KEY);
    }

    @Bean
    public WebClientInitializer applicationStartupListener(RequestLogger requestLogger, MetricsReporter metricsReporter, ReactiveCircuitBreakerFactory reactiveCircuitBreakerFactory) {
        return new WebClientInitializer(requestLogger, metricsReporter, reactiveCircuitBreakerFactory);
    }

    @Bean
    public RequestLogger requestLogger(LoggingConfig loggingConfig) {
        return new DefaultRequestLogger(loggingConfig, applicationName);
    }

    @Bean
    @ConditionalOnClass(MeterRegistry.class)
    public MetricsReporter metricsReporter(MeterRegistry meterRegistry, MetricsConfig metricsConfig) {
        if(metricsConfig.getPrefix() != null) {
            return new DefaultMetricsReporter(meterRegistry, metricsConfig, applicationName);
        } else {
            return new DisabledMetricsReporter();
        }
    }

    @Bean
    @ConditionalOnMissingClass("io.micrometer.core.instrument.MeterRegistry")
    public MetricsReporter metricsReporter() {
        return new DisabledMetricsReporter();
    }

    // Exposes the full RequestProcessor
    @Bean("requestProcessor")
    public RequestProcessor requestProcessor(RequestLogger requestLogger, MetricsReporter metricsReporter, List<ErrorAttributesProvider> errorAttributesProviders) {
        return new RequestProcessorImpl(requestLogger, metricsReporter, errorAttributesProviders);
    }

    // Exposes the RequestProcessor as a limited Rest version
    @Bean("restRequestProcessor")
    public RestRequestProcessor restRequestProcessor(RequestProcessor requestProcessor) {
        return requestProcessor;
    }

    // Exposes the RequestProcessor as a limited Internal version
    @Bean("internalRequestProcessor")
    public InternalRequestProcessor internalRequestProcessor(RequestProcessor requestProcessor) {
        return requestProcessor;
    }

    @Bean
    public ErrorAttributesProvider traceLoggerProvider() {
        return new TraceLoggerProvider();
    }

    @Bean
    public ErrorAttributesProvider applicationNameProvider() {
        return new ApplicationNameProvider(applicationName);
    }
}
