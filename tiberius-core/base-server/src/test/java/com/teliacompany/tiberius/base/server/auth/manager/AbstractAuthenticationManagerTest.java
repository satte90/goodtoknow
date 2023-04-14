package com.teliacompany.tiberius.base.server.auth.manager;

import com.teliacompany.webflux.request.RequestProcessor;
import com.teliacompany.webflux.request.config.LoggingConfig;
import com.teliacompany.webflux.request.log.DefaultRequestLogger;
import com.teliacompany.webflux.request.metrics.DisabledMetricsReporter;
import com.teliacompany.webflux.request.processor.RequestProcessorImpl;
import com.teliacompany.webflux.request.processor.error.ApplicationNameProvider;
import com.teliacompany.webflux.request.processor.error.ErrorAttributesProvider;
import com.teliacompany.webflux.request.processor.error.TraceLoggerProvider;
import com.teliacompany.tiberius.base.server.auth.TiberiusJwtAuthenticationToken;
import com.teliacompany.tiberius.base.server.auth.key.DefaultPublicKeyProvider;
import com.teliacompany.tiberius.base.server.auth.key.PublicKeyProvider;
import com.teliacompany.tiberius.base.server.auth.manager.mock.MockedPublicKeyProvider;
import com.teliacompany.tiberius.base.server.config.security.DefaultTiberiusSecurityConfigurer;
import com.teliacompany.tiberius.base.server.config.security.TiberiusSecurityConfigurer;
import com.teliacompany.tiberius.user.api.v1.Role;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.web.reactive.context.StandardReactiveWebEnvironment;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.security.core.Authentication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class AbstractAuthenticationManagerTest {
    protected static MockedPublicKeyProvider mockedKeyProvider;

    RequestProcessor requestProcessor;
    ConfigurableEnvironment testEnv;
    List<TiberiusSecurityConfigurer> securityConfigurers;
    List<PublicKeyProvider> publicKeyProviders;

    abstract TiberiusAuthenticationManager createManager();

    @BeforeEach
    public void setUp() {
        String applicationName = "test-app-name";
        final List<ErrorAttributesProvider> errorAttributesProviders = List.of(new ApplicationNameProvider(applicationName), new TraceLoggerProvider());

        requestProcessor = new RequestProcessorImpl(new DefaultRequestLogger(new LoggingConfig(), applicationName), new DisabledMetricsReporter(), errorAttributesProviders);
        testEnv = new StandardReactiveWebEnvironment();

        securityConfigurers = new ArrayList<>();
        publicKeyProviders = new ArrayList<>();

        if(mockedKeyProvider == null) {
            mockedKeyProvider = new MockedPublicKeyProvider("123");
        }
        publicKeyProviders.add(new DefaultPublicKeyProvider(null));
        publicKeyProviders.add(mockedKeyProvider);

        securityConfigurers.add(new DefaultTiberiusSecurityConfigurer("/api-docs"));
    }

    static void assertTiberiusJwtTokenAuthentication(Authentication finalAuth, String tcad) {
        assertTrue(finalAuth.isAuthenticated());
        assertTrue(finalAuth instanceof TiberiusJwtAuthenticationToken);
        TiberiusJwtAuthenticationToken jwtAuth = (TiberiusJwtAuthenticationToken) finalAuth;
        assertEquals(tcad, jwtAuth.getTcad());
    }

    static void assertTiberiusJwtTokenAuthenticationRole(Authentication finalAuth, Role... expectedRoles) {
        assertEquals(expectedRoles.length, finalAuth.getAuthorities().size());
        Arrays.stream(expectedRoles)
                .map(Role::name)
                .forEach(expectedRole -> {
                    //For each expected role, assert an authority exist for it
                    assertTrue(finalAuth.getAuthorities().stream().anyMatch(r -> r.getAuthority().equals(expectedRole)));
                });
    }


}
