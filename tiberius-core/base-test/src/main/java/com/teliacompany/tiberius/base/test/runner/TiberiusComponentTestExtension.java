package com.teliacompany.tiberius.base.test.runner;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.teliacompany.tiberius.base.test.exception.TestRuntimeException;
import com.teliacompany.tiberius.base.test.kafka.ComponentTestKafkaConsumer;
import com.teliacompany.tiberius.base.test.mock.ApiMarketMock;
import com.teliacompany.tiberius.base.test.mock.ApigeeMock;
import com.teliacompany.tiberius.base.test.mock.SpockAuthMock;
import com.teliacompany.tiberius.base.test.utils.approvals.ApprovalsConfigurer;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.Method;

public class TiberiusComponentTestExtension implements BeforeAllCallback, BeforeEachCallback {
    private ComponentTestKafkaConsumer kafkaConsumer;

    @Override
    public void beforeAll(ExtensionContext extensionContext) {
        Class<?> testClass = extensionContext.getTestClass().orElseThrow(() -> new TestRuntimeException("No test class!!!"));
        TiberiusTestAppBootstrapper bootstrapperSingleton = TiberiusTestAppBootstrapper.instance(testClass);

        bootstrapperSingleton.startWiremockIfNotRunning();
        bootstrapperSingleton.mockApiMarketAuthentication();
        bootstrapperSingleton.mockApigeeAuthentication();
        bootstrapperSingleton.mockSpockAuthentication();
        bootstrapperSingleton.startMongoIfNotRunningAndEnabled();
        bootstrapperSingleton.startKafkaIfNotRunningAndEnabled();
        bootstrapperSingleton.startAppIfNotRunning();
        bootstrapperSingleton.validateApplicationName();
        bootstrapperSingleton.enableTestMode();
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
        if(kafkaConsumer != null) {
            kafkaConsumer.close();
        }

        Class<?> testClass = extensionContext.getTestClass().orElseThrow(() -> new RuntimeException("No test class!!!"));
        String methodName = extensionContext.getTestMethod().map(Method::getName).orElse("Unknown Test Method");
        ApprovalsConfigurer.configure();
        final Object testInstance = extensionContext.getTestInstance().orElseThrow(() -> new TestRuntimeException("No test instance found"));
        TiberiusTestAppBootstrapper.injectWebTestClient(testInstance);
        kafkaConsumer = TiberiusTestAppBootstrapper.injectKafkaConsumer(testInstance);

        TiberiusTestAppBootstrapper bootstrapperSingleton = TiberiusTestAppBootstrapper.instance(testClass);
        bootstrapperSingleton.clearMongo();
        bootstrapperSingleton.setAndLogTestMethod(testClass.getSimpleName(), methodName);

        resetWireMock();
        bootstrapperSingleton.mockApiMarketAuthentication();
        bootstrapperSingleton.mockApigeeAuthentication();
        bootstrapperSingleton.mockSpockAuthentication();
    }

    public static TiberiusTestAppBootstrapper getTestAppInstance() {
        return TiberiusTestAppBootstrapper.getCurrentInstance();
    }


    private void resetWireMock() {
        WireMock.reset();
        ApiMarketMock.mockAuthentication();
        ApigeeMock.mockAuthentication();
        SpockAuthMock.mockAuthentication();
    }
}
