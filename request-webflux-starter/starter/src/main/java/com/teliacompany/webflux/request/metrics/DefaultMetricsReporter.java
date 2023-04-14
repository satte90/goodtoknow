package com.teliacompany.webflux.request.metrics;

import com.teliacompany.webflux.request.context.RequestContext;
import com.teliacompany.webflux.error.exception.WebException;
import com.teliacompany.webflux.request.config.MetricsConfig;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DefaultMetricsReporter implements MetricsReporter {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultMetricsReporter.class);
    private static final String SERVICE_NAME_TAG = "serviceName";
    private static final String ADDRESS_TAG = "address";
    private static final String ERROR_TAG = "error";
    private static final String RESPONSE_CODE_TAG = "response_code";
    private final MeterRegistry meterRegistry;
    private final MetricsConfig metricsConfig;
    private final String applicationName;

    public DefaultMetricsReporter(MeterRegistry meterRegistry,
                                  MetricsConfig metricsConfig,
                                  String applicationName) {
        this.meterRegistry = meterRegistry;
        this.metricsConfig = metricsConfig;
        this.applicationName = applicationName;
    }

    @PostConstruct
    public void init() {
        LOG.info("Default Metrics reporting enabled for application: {}", applicationName);
    }

    /**
     * Reports request duration in seconds on the format prefix.application_name.request_duration
     */
    @Override
    public void reportRequestData(RequestContext requestContext, HttpStatus httpStatus) {
        final String serviceName = requestContext.getServiceName() != null ? requestContext.getServiceName() : applicationName;
        final List<Tag> tags = getTags(serviceName, httpStatus, false);
        meterRegistry.timer(getMetricName(), tags).record(requestContext.getRequestDuration(), TimeUnit.MILLISECONDS);
    }

    /**
     * Reports occurrence of an error response on the format prefix.application_name.service_called_name.request_failure. Tagged by address and response status code.
     */
    @Override
    public void reportRequestError(RequestContext requestContext, ClientResponse clientResponse) {
        final String serviceName = requestContext.getServiceName() != null ? requestContext.getServiceName() : applicationName;
        final List<Tag> tags = getTags(serviceName, clientResponse.statusCode(), true);
        meterRegistry.timer(getMetricName(), tags).record(requestContext.getRequestDuration(), TimeUnit.MILLISECONDS);
    }

    @Override
    public void reportRequestError(RequestContext requestContext, Throwable e) {
        final String serviceName = requestContext.getServiceName() != null ? requestContext.getServiceName() : applicationName;
        final HttpStatus statusCode = e instanceof WebException ? ((WebException) e).getStatus() : HttpStatus.INTERNAL_SERVER_ERROR;
        final List<Tag> tags = getTags(serviceName, statusCode, true);
        meterRegistry.timer(getMetricName(), tags).record(requestContext.getRequestDuration(), TimeUnit.MILLISECONDS);
    }

    private String getMetricName() {
        return String.format(Locale.ROOT, "%s.%s.%s", metricsConfig.getPrefix(), applicationName, REQUEST_DURATION_STAT);
    }

    private static List<Tag> getTags(String serviceName, HttpStatus status, boolean error) {
        return Arrays.asList(
                Tag.of(SERVICE_NAME_TAG, serviceName != null ? serviceName : "n/a"),
//                Tag.of(ADDRESS_TAG, address != null ? address : "n/a"), // Do not enable this as it apparently will create unique metrics entries for each unique url, if for example with different path variables
                Tag.of(RESPONSE_CODE_TAG, status != null ? String.valueOf(status.value()) : "n/a"),
                Tag.of(ERROR_TAG, String.valueOf(error))
        );
    }
}
