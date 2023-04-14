package com.teliacompany.tiberius.base.server.service.smoketest;

import com.teliacompany.webflux.error.ErrorAttributesUtils;
import com.teliacompany.tiberius.base.server.api.smoketest.SmokeTestStatus;
import com.teliacompany.tiberius.base.server.api.smoketest.SmokeTestSubServiceResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;

import java.util.function.Function;
import java.util.function.Supplier;

public class SmokeTest<RES> {
    private static final Logger LOG = LoggerFactory.getLogger(SmokeTest.class);

    private final String subServiceName;
    private final Class<RES> expectedResponseClass;
    private long slowThreshold = 1000;
    private Supplier<Mono<RES>> smokeTestFunction;
    private Function<RES, Boolean> dataValidatorFunction;
    private SmokeTestRequest smokeTestRequest = new SmokeTestRequest("Unknown");

    public SmokeTest(String subServiceName, Class<RES> expectedResponseClass) {
        this.subServiceName = subServiceName;
        this.expectedResponseClass = expectedResponseClass;
        this.smokeTestFunction = Mono::empty;
        this.dataValidatorFunction = x -> true;
    }

    public Mono<SmokeTestSubServiceResult> run() {
        LOG.debug("Running smoke test to sub system {}, expecting to get {} back", subServiceName, expectedResponseClass);
        long start = System.currentTimeMillis();
        return smokeTestFunction.get()
                .map(response -> {
                    long ms = System.currentTimeMillis() - start;
                    boolean dataValid = Boolean.TRUE.equals(dataValidatorFunction.apply(response));
                    return new SmokeTestSubServiceResult()
                            .setName(subServiceName)
                            .setResponseTime(ms)
                            .setStatus(getStatus(ms))
                            .setValidData(dataValid);
                })
                .switchIfEmpty(Mono.just(new SmokeTestSubServiceResult()
                        .setName(subServiceName)
                        .setStatus(SmokeTestStatus.NOT_CHECKED)))
                .onErrorResume(e -> {
                    SmokeTestSubServiceResult error = new SmokeTestSubServiceResult()
                            .setName(subServiceName)
                            .setResponseTime(System.currentTimeMillis() - start)
                            .setStatus(SmokeTestStatus.FAILED)
                            .setValidData(false)
                            .setError(ErrorAttributesUtils.getWebException(e)
                                    .map(ex -> ErrorAttributesUtils.buildFromWebException(ex, smokeTestRequest.getAppName()))
                                    .orElse(ErrorAttributesUtils.buildFromGenericException(e, HttpStatus.INTERNAL_SERVER_ERROR, "appNameTest")));
                    return Mono.just(error);
                });
    }

    private SmokeTestStatus getStatus(Long ms) {
        return ms < slowThreshold ? SmokeTestStatus.OK : SmokeTestStatus.SLOW;
    }

    public SmokeTest<RES> setSlowThreshold(long slowThreshold) {
        this.slowThreshold = slowThreshold;
        return this;
    }

    public SmokeTest<RES> setSmokeTestFunction(Supplier<Mono<RES>> smokeTestFunction) {
        this.smokeTestFunction = smokeTestFunction;
        return this;
    }

    public SmokeTest<RES> setDataValidatorFunction(Function<RES, Boolean> dataValidatorFunction) {
        this.dataValidatorFunction = dataValidatorFunction;
        return this;
    }

    public SmokeTest<RES> setSmokeTestRequest(SmokeTestRequest smokeTestRequest) {
        this.smokeTestRequest = smokeTestRequest;
        return this;
    }
}
