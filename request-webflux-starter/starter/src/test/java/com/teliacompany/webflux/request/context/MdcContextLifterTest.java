package com.teliacompany.webflux.request.context;

import com.teliacompany.webflux.request.RequestProcessor;
import com.teliacompany.webflux.request.config.RequestWebfluxStarterAutoConfiguration;
import com.teliacompany.webflux.request.log.DisabledRequestLogger;
import com.teliacompany.webflux.request.metrics.DisabledMetricsReporter;
import com.teliacompany.webflux.request.processor.InternalRequestProcessor;
import com.teliacompany.webflux.request.processor.RequestProcessorImpl;
import com.teliacompany.webflux.request.processor.error.ApplicationNameProvider;
import com.teliacompany.webflux.request.processor.error.ErrorAttributesProvider;
import com.teliacompany.webflux.request.processor.error.TraceLoggerProvider;
import com.teliacompany.webflux.request.processor.model.ProcessInternalRequestData;
import com.teliacompany.webflux.request.utils.Constants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import reactor.core.publisher.Mono;

import java.util.List;

public class MdcContextLifterTest {
    @Test
    public void testMdcDataLifted() {
        //Register hooks for reactor
        final String applicationName = "test-app-name";
        new RequestWebfluxStarterAutoConfiguration(applicationName).contextOperatorHook();

        final List<ErrorAttributesProvider> errorAttributesProviders = List.of(new ApplicationNameProvider(applicationName), new TraceLoggerProvider());
        InternalRequestProcessor requestProcessor = new RequestProcessorImpl(new DisabledRequestLogger(), new DisabledMetricsReporter(), errorAttributesProviders);
        ProcessInternalRequestData requestData = new ProcessInternalRequestData("myTid", "bnd007", "777");
        requestProcessor.processInternal(requestData)
                .withHandler(() -> {
                    //Assert initial data is set
                    Assertions.assertEquals("myTid", MDC.get(Constants.MDC_TRANSACTION_ID_KEY));
                    Assertions.assertEquals("bnd007", MDC.get(Constants.MDC_TCAD_KEY));
                    Assertions.assertEquals("777", MDC.get(Constants.MDC_TSCID_KEY));

                    return RequestProcessor.getTransactionContext()
                            .map(transactionContext -> {
                                //Assert values exist in transaction context at this point (Otherwise MDC cannot be automatically updated)
                                Assertions.assertEquals("myTid", transactionContext.getTid());
                                Assertions.assertEquals("bnd007", transactionContext.getTcad());
                                Assertions.assertEquals("777", transactionContext.getTscid());

                                // Also reassure MDC still has the data:
                                assertMdcContent();
                                clearMdcAndVerifyItIsCleared();
                                return "Step 1 done";
                            })
                            .map(msg -> {
                                //Assert MDC is re-populated after a map operation
                                assertMdcContent();
                                clearMdcAndVerifyItIsCleared();
                                return "Step 2 done";
                            })
                            .log()
                            .flatMap(m -> Mono.defer(() -> {
                                assertMdcContent();
                                clearMdcAndVerifyItIsCleared();
                                return Mono.just("21321");
                            }))
                            .flatMap(msg -> {
                                //Assert MDC is re-populated after a flat map operation
                                assertMdcContent();
                                clearMdcAndVerifyItIsCleared();
                                return Mono.empty();
                            })
                            .switchIfEmpty(Mono.defer(() -> {
                                //Assert MDC is re-populated after an empty mono
                                assertMdcContent();
                                clearMdcAndVerifyItIsCleared();
                                return RequestProcessor.getTransactionContext()
                                        .flatMap(tx -> {
                                            Assertions.assertNotNull(tx);
                                            assertMdcContent();
                                            clearMdcAndVerifyItIsCleared();
                                            return Mono.empty();
                                        });
                            }))
                            .defaultIfEmpty("Step 3 Done")
                            .map(msg -> {
                                assertMdcContent();
                                clearMdcAndVerifyItIsCleared();
                                throw new RuntimeException("oh no 1");
                            })
                            .onErrorResume(t -> {
                                assertMdcContent();
                                clearMdcAndVerifyItIsCleared();
                                throw new RuntimeException("oh no 2");
                            })
                            .onErrorContinue((t, y) -> {
                                assertMdcContent();
                                clearMdcAndVerifyItIsCleared();
                                throw new RuntimeException("oh no 3");
                            })
                            .onErrorMap(t1 -> {
                                assertMdcContent();
                                clearMdcAndVerifyItIsCleared();
                                return new RuntimeException("oh no 4");
                            })
                            .onErrorReturn("Done with those errors")
                            .flatMap(m -> {
                                assertMdcContent();
                                clearMdcAndVerifyItIsCleared();
                                return Mono.error(new RuntimeException("oh no 5"));
                            })
                            .onErrorResume(t -> Mono.just("DONE"));
                })
                .map(r -> "done")
                .block();
    }

    private static void assertMdcContent() {
        Assertions.assertEquals("myTid", MDC.get(Constants.MDC_TRANSACTION_ID_KEY));
        Assertions.assertEquals("bnd007", MDC.get(Constants.MDC_TCAD_KEY));
        Assertions.assertEquals("777", MDC.get(Constants.MDC_TSCID_KEY));
    }

    private static void clearMdcAndVerifyItIsCleared() {
        MDC.clear();

        //Assert MDC is cleared
        Assertions.assertNull(MDC.get(Constants.MDC_TRANSACTION_ID_KEY));
        Assertions.assertNull(MDC.get(Constants.MDC_TCAD_KEY));
        Assertions.assertNull(MDC.get(Constants.MDC_TSCID_KEY));
    }
}
