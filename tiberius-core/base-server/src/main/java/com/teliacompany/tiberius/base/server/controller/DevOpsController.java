package com.teliacompany.tiberius.base.server.controller;

import com.teliacompany.webflux.request.RequestProcessor;
import com.teliacompany.tiberius.base.server.config.RegistrationProperties;
import com.teliacompany.tiberius.base.server.secrets.agent.SecretAgent;
import com.teliacompany.tiberius.base.server.service.DevOpsService;
import com.teliacompany.tiberius.base.server.service.smoketest.DefaultSmokeTestService;
import com.teliacompany.tiberius.base.server.service.smoketest.SmokeTestRequestConverter;
import com.teliacompany.tiberius.base.server.service.smoketest.SmokeTestService;
import io.swagger.v3.oas.annotations.Hidden;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("devops")
@Hidden
public class DevOpsController {
    private static final Logger LOG = LoggerFactory.getLogger(DevOpsController.class);
    private final RequestProcessor requestProcessor;
    private final DevOpsService devOpsService;
    private final boolean serviceRegistrationEnabled;
    private final String appName;
    private final SmokeTestService smokeTestService;
    private final SecretAgent tiberiusSecretAgent;
    private int periodicRegistrationsMade = 0;

    public DevOpsController(RequestProcessor requestProcessor,
                            DevOpsService devOpsService,
                            RegistrationProperties registrationProperties,
                            List<SmokeTestService> smokeTestServices,
                            @Value("${spring.application.name}") String appName,
                            SecretAgent tiberiusSecretAgent) {
        this.requestProcessor = requestProcessor;
        this.devOpsService = devOpsService;
        this.serviceRegistrationEnabled = registrationProperties.isServiceRegistrationEnabled();
        this.appName = appName;

        this.smokeTestService = smokeTestServices.stream()
                .filter(sts -> !(sts instanceof DefaultSmokeTestService))
                .findFirst()
                .orElse(new DefaultSmokeTestService());
        this.tiberiusSecretAgent = tiberiusSecretAgent;
    }

    @GetMapping(name = "versions", path = "/versions")
    public Mono<ResponseEntity<Object>> versions(ServerHttpRequest request) {
        return requestProcessor.process(request)
                .withoutRequestBody()
                .withHandler(devOpsService::versions);
    }

    @GetMapping(name = "register", path = "/register")
    public Mono<ResponseEntity<Object>> register(ServerHttpRequest request) {
        return requestProcessor.process(request)
                .withRequestObject(LOG)
                .withHandler(devOpsService::register);
    }

    @GetMapping(name = "unregister", path = "/unregister")
    public Mono<ResponseEntity<Object>> unregister(ServerHttpRequest request) {
        return requestProcessor.process(request)
                .withoutRequestBody()
                .withHandler(devOpsService::unregister);
    }

    @Scheduled(fixedRate = 3600000, initialDelay = 300000)
    public void periodicallyRegister() {
        requestProcessor.processInternal()
                .withHandler(() -> {
                    //Reduce registrations after 48 hours to once per day, until that point once every hour (3600000ms)
                    boolean registrationBackOff = periodicRegistrationsMade <= 48 || periodicRegistrationsMade % 24 == 0;
                    if(serviceRegistrationEnabled && registrationBackOff) {
                        periodicRegistrationsMade++;
                        return devOpsService.register(LOG);
                    }
                    return Mono.just("Registration ignored");
                }).subscribe();
    }

    @GetMapping(name = "listConfig", path = "config")
    public Mono<ResponseEntity<Object>> listConfig(ServerHttpRequest request) {
        return requestProcessor.process(request)
                .withoutRequestBody()
                .withHandler(devOpsService::getApplicationConfig);
    }

    @PutMapping(name = "setLogLevel", path = "log/level/{level}")
    public Mono<ResponseEntity<Object>> setLogLevel(ServerHttpRequest request,
                                                    @PathVariable String level,
                                                    @RequestParam(required = false, defaultValue = "com.teliacompany.tiberius") String loggerName) {
        return requestProcessor.process(request)
                .withoutRequestBody()
                .withHandler(() -> devOpsService.setLogLevel(level, loggerName));
    }

    @GetMapping(name = "logMessage", path = "log/level/{level}/{message}")
    public Mono<ResponseEntity<Object>> logMessage(ServerHttpRequest request,
                                                   @PathVariable String level,
                                                   @PathVariable String message,
                                                   @RequestParam(required = false) String loggerName) {
        return requestProcessor.process(request)
                .withoutRequestBody()
                .withHandler(() -> devOpsService.log(level, loggerName, message));
    }

    @GetMapping(name = "appName", path = "/name")
    public Mono<ResponseEntity<Object>> getAppName(ServerHttpRequest request) {
        return requestProcessor.process(request)
                .withoutRequestBody()
                .withHandler(() -> Mono.just(appName));
    }

    @GetMapping(name = "smoketest", path = "smoketest")
    public Mono<ResponseEntity<Object>> smokeTest(ServerHttpRequest request) {
        return requestProcessor.process(request)
                .withRequestObject(appName)
                .withRequestConverter(SmokeTestRequestConverter::convert)
                .withHandler(smokeTestService::test);
    }

    @GetMapping(name = "refreshSecrets", path = "secrets/refresh")
    public Mono<ResponseEntity<Object>> refreshSecrets(ServerHttpRequest request) {
        return requestProcessor.process(request)
                .withoutRequestBody()
                .withHandler(tiberiusSecretAgent::refreshSecrets);
    }

    @GetMapping(path = "dependencies", produces = APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Object>> getDependencies(ServerHttpRequest serverHttpRequest) {
        return requestProcessor.process(serverHttpRequest)
                .withoutRequestBody()
                .withHandler(devOpsService::readDependenciesFile);
    }
}
