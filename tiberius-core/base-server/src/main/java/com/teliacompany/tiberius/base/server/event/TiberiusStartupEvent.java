package com.teliacompany.tiberius.base.server.event;

import com.teliacompany.tiberius.base.server.auth.manager.TiberiusAuthenticationManager;
import com.teliacompany.tiberius.base.server.config.VersionProperties;
import com.teliacompany.tiberius.base.server.secrets.agent.SecretAgent;
import com.teliacompany.tiberius.base.server.service.DevOpsService;
import com.teliacompany.webflux.request.RequestProcessor;
import com.teliacompany.webflux.request.processor.InternalRequestProcessor;
import com.teliacompany.webflux.request.processor.model.ProcessInternalRequestData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.web.reactive.context.StandardReactiveWebEnvironment;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import reactor.core.publisher.Mono;

import java.security.PublicKey;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Configuration(value = "TiberiusStartup")
public class TiberiusStartupEvent implements ApplicationListener<ApplicationStartedEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(TiberiusStartupEvent.class);
    private static final Logger STARTUP_LOG = LoggerFactory.getLogger("com.teliacompany.tiberius.startup.Logger");
    public static final int MAX_LOADER_RETRIES = 10;

    private final RequestProcessor requestProcessor;

    private final StandardReactiveWebEnvironment springEnv;
    private final TiberiusAuthenticationManager tiberiusAuthenticationManager;
    private final DevOpsService devOpsService;
    private final VersionProperties versionProperties;

    private final InternalRequestProcessor internalRequestProcessor;

    private final SecretAgent secretAgent;

    private String hostName;
    private boolean publicKeyLoaded = false;
    private boolean secretsLoaded = false;
    private int loaderRetries = 0;
    private ConfigurableApplicationContext appContext;

    public TiberiusStartupEvent(RequestProcessor requestProcessor,
                                StandardReactiveWebEnvironment springEnv,
                                TiberiusAuthenticationManager tiberiusAuthenticationManager,
                                DevOpsService devOpsService,
                                VersionProperties versionProperties,
                                InternalRequestProcessor internalRequestProcessor,
                                SecretAgent secretAgent) {
        this.requestProcessor = requestProcessor;
        this.springEnv = springEnv;
        this.tiberiusAuthenticationManager = tiberiusAuthenticationManager;
        this.devOpsService = devOpsService;
        this.versionProperties = versionProperties;
        this.internalRequestProcessor = internalRequestProcessor;
        this.secretAgent = secretAgent;
    }

    @Override
    public void onApplicationEvent(ApplicationStartedEvent applicationReadyEvent) {
        System.setProperty("application.version", versionProperties.getAppVersion());
        ProcessInternalRequestData requestData = new ProcessInternalRequestData("onAppStart-" + UUID.randomUUID());

        this.hostName = Optional.ofNullable(System.getenv("HOSTNAME")).orElse("unknown-hostname-" + UUID.randomUUID());
        this.appContext = applicationReadyEvent.getApplicationContext();
        LOG.info("On application startup");

        internalRequestProcessor.processInternal(requestData)
                .withRequestObject(hostName)
                .withMetaData((v, h) -> new HashMap<>(Map.of("hostName", hostName)))
                .withHandler(this::runStartupRequests)
                .block(Duration.ofSeconds(180)); // give it 2.5 mins we don't really want this block to hit, rather let underlying webclients timeout to throw real error

        final String loggingConfig = springEnv.getProperty("logging.config", "classpath:log4j2.xml");
        if(!loggingConfig.endsWith("-local.xml")) {
            //If we are running snapshot version (i.e. locally probably) and we are using a non dev/componenttest profile such as sit/at/prod
            STARTUP_LOG.info("\nApplication started successfully!\n" +
                            "Since logging config={} console logging is disabled.\n" +
                            "Further logging will be found in server/log folder in the repo\n",
                    loggingConfig
            );
        }

        LOG.info("On application startup completed");
    }

    public Mono<String> runStartupRequests(String hostName) {
        return createContext(hostName)
                .flatMap(this::fetchLatestPublicKey)
                .flatMap(this::fetchSecrets)
                .flatMap(this::runDevopsOnStartup)
                .then(Mono.just("onAppStart Done"));
    }

    @Scheduled(initialDelay = 3, fixedDelay = 1, timeUnit = TimeUnit.MINUTES)
    public void periodicallyRefresh() {
        if(!publicKeyLoaded || !secretsLoaded) {
            this.loaderRetries++;
            if(loaderRetries > MAX_LOADER_RETRIES) {
                LOG.error("Could not fetch required public key and/or secrets, service will not work as expected. Shutting down");
                SpringApplication.exit(appContext, () -> 1);
            }
            requestProcessor.processInternal()
                    .withHandler(this::refreshKeyAndSecrets)
                    .subscribe();
        }
    }

    private Mono<String> refreshKeyAndSecrets() {
        return createContext(hostName)
                .map(ctx -> {
                    LOG.info("Refreshing key and/or secrets as these has not successfully been loaded. (Retry {}/{})", this.loaderRetries, MAX_LOADER_RETRIES);
                    return ctx;
                })
                .flatMap(this::fetchLatestPublicKey)
                .flatMap(this::fetchSecrets)
                .then(Mono.just("Key and Secrets refresh Completed"));
    }

    public Mono<StartupContext> createContext(String hostName) {
        return Mono.just(new StartupContext(hostName));
    }

    public Mono<StartupContext> fetchLatestPublicKey(StartupContext context) {
        return tiberiusAuthenticationManager.getLatestPublicKey()
                .map(keyResponse -> {
                    // Key response may be success but have a null key, for example if authentication is disabled and local secrets are used, then we don't need a key
                    this.publicKeyLoaded = keyResponse.isSuccess();
                    return context.returnWithPublicKey(keyResponse.getPublicKey());
                })
                .switchIfEmpty(Mono.just(context)); // publicKeyLoaded will remain false
    }

    private Mono<StartupContext> fetchSecrets(StartupContext context) {
        return secretAgent.fetchSecrets(context.publicKey)
                .map(isSuccess -> {
                    secretsLoaded = isSuccess;
                    return context;
                })
                .switchIfEmpty(Mono.defer(() -> {
                    // if secretAgent returns empty (not false) then treat it as success. *No secrets were fetched but that is fine*
                    secretsLoaded = true;
                    return Mono.just(context);
                }));
    }

    private Mono<StartupContext> runDevopsOnStartup(StartupContext context) {
        return devOpsService.onStartup(context.hostName)
                .thenReturn(context);
    }

    private static class StartupContext {
        private String hostName;
        private PublicKey publicKey;

        public StartupContext(String hostName) {
            this.hostName = hostName;
        }

        public StartupContext returnWithPublicKey(PublicKey publicKey) {
            this.publicKey = publicKey;
            return this;
        }
    }
}
