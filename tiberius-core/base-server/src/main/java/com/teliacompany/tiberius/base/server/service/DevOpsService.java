package com.teliacompany.tiberius.base.server.service;

import com.teliacompany.tiberius.base.server.api.BaseErrors;
import com.teliacompany.tiberius.base.server.api.ServiceRegistryEntry;
import com.teliacompany.tiberius.base.server.api.VersionsResponse;
import com.teliacompany.tiberius.base.server.config.ApplicationProperties;
import com.teliacompany.tiberius.base.server.config.IntegrationUsageStatuses;
import com.teliacompany.tiberius.base.server.config.VersionProperties;
import com.teliacompany.tiberius.base.server.integration.praefectus.PraefectusClient;
import com.teliacompany.tiberius.base.server.integration.slack.DevOpsSlackClient;
import com.teliacompany.tiberius.base.server.util.BaseServerUtils;
import com.teliacompany.webflux.error.exception.client.BadRequestException;
import com.teliacompany.webflux.error.exception.client.ForbiddenException;
import com.teliacompany.webflux.error.exception.client.NotFoundException;
import com.teliacompany.webflux.request.RequestProcessor;
import com.teliacompany.webflux.request.client.WebClient;
import com.teliacompany.webflux.request.client.WebClientRegistry;
import com.teliacompany.webflux.request.processor.InternalRequestProcessor;
import com.teliacompany.webflux.request.processor.model.ProcessInternalRequestData;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.text.CaseUtils;
import org.apache.commons.text.WordUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.spi.AbstractLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.context.StandardReactiveWebEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class DevOpsService {
    private static final Logger LOG = LoggerFactory.getLogger(DevOpsService.class);
    private static final Logger STARTUP_LOG = LoggerFactory.getLogger("com.teliacompany.tiberius.startup.Logger");
    private static final List<String> BLACKLISTED_PROPERTY_SOURCES = Arrays.asList("systemProperties", "server.ports", "commandLineArgs", "springCloudDefaultProperties", "systemEnvironment", "springCloudClientHostInfo");
    private static final List<String> BLACKLISTED_PROP_KEYS = Arrays.asList(".key", "secret", "password", "code", "token", "pwd", ".pass");
    private static final List<Level> LOGGABLE_LEVELS = Arrays.asList(Level.TRACE, Level.DEBUG, Level.INFO, Level.WARN, Level.ERROR);
    private static final ResourceLoader RESOURCE_LOADER = new DefaultResourceLoader();

    private final VersionProperties versionProperties;
    private final PraefectusClient praefectusClient;
    private final String environmentProfile;
    private final InternalRequestProcessor requestProcessor;
    private final String ip;
    private final DevOpsSlackClient slackClient;
    private final String serviceName;
    private final Instant buildTime;
    private final Integer port;
    private final boolean serviceRegistrationEnabled;

    private final StandardReactiveWebEnvironment springEnv;
    private final IntegrationUsageStatuses integrationUsageStatuses;

    private String hostName;
    private long lastLogged = System.currentTimeMillis();
    private List<String> integrations;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public DevOpsService(VersionProperties versionProperties,
                         PraefectusClient praefectusClient,
                         DevOpsSlackClient slackClient,
                         ApplicationProperties applicationConfig,
                         InternalRequestProcessor internalRequestProcessor,
                         StandardReactiveWebEnvironment springEnv) {
        this.versionProperties = versionProperties;
        this.praefectusClient = praefectusClient;
        this.slackClient = slackClient;
        this.serviceName = applicationConfig.getApplicationName();
        this.environmentProfile = applicationConfig.getActiveSpringProfiles().isEmpty() ? "unknown" : applicationConfig.getActiveSpringProfiles().get(0);
        this.requestProcessor = internalRequestProcessor;
        this.serviceRegistrationEnabled = applicationConfig.getRegistrationProperties().isServiceRegistrationEnabled();
        this.springEnv = springEnv;
        this.integrationUsageStatuses = applicationConfig.getIntegrationUsageStatuses();
        this.buildTime = applicationConfig.getBuildProperties().getBuildTime();

        ip = BaseServerUtils.getIp();
        port = applicationConfig.getServerPort();
    }

    public Mono<String> onStartup(String hostName) {
        this.hostName = hostName;

        final Set<String> integrationsSet = WebClientRegistry.stream()
                .map(WebClient::getServiceName)
                .filter(StringUtils::isNotBlank)
                .map(name -> {
                    if(StringUtils.containsAny(name, "_", "-", " ", ".")) {
                        return CaseUtils.toCamelCase(name, true, '_', ' ', '-', '.');
                    }
                    return name; //Else assume already CamelCase
                })
                .collect(Collectors.toSet());
        if(integrationUsageStatuses.isHazelcastEnabled()) {
            integrationsSet.add("Hazelcast");
        }
        if(integrationUsageStatuses.isMongodbEnabled()) {
            integrationsSet.add("MongoDB");
        }
        if(integrationUsageStatuses.isLdapEnabled()) {
            integrationsSet.add("Ldap");
        }
        if(integrationUsageStatuses.isNonMongoDatabaseEnabled()) {
            integrationsSet.add(integrationUsageStatuses.getNonMongoDatabaseUsed());
        }
        if(integrationUsageStatuses.isKafkaEnabled()) {
            integrationsSet.add("Kafka");
        }
        this.integrations = new ArrayList<>(integrationsSet);
        LOG.info("Integrations Used: {}", this.integrations);

        return Mono.zipDelayError(slackClient.postStartupMessage(), this.register(STARTUP_LOG))
                .then(Mono.just("ok"));
    }

    public void onShutdown() {
        STARTUP_LOG.info("Got shut down signal");
        String transactionId = "onShutdown-" + UUID.randomUUID();
        ProcessInternalRequestData requestData = new ProcessInternalRequestData(transactionId);

        requestProcessor.processInternal(requestData)
                .withHandler(() -> Mono.zipDelayError(slackClient.postShutdownMessage(), this.unregister()))
                .subscribe(r -> {
                    STARTUP_LOG.info("\033[0;33mShutdown slack message status: {} \033[0m", r.getT1());
                    STARTUP_LOG.info("\033[0;33mPraefectus unregister status: {} \033[0m", r.getT2());
                });
        STARTUP_LOG.info("Forwarded shutdown messages");
    }

    public Mono<VersionsResponse> versions() {
        return Mono.just(getVersionsResponse());
    }

    public Mono<String> register(Logger logger) {
        if(serviceRegistrationEnabled) {
            logger.info("Registering Service with Tiberius Praefectus\n");

            ServiceRegistryEntry entry = new ServiceRegistryEntry()
                    .setTimestamp(System.currentTimeMillis())
                    .setEnvironment(environmentProfile)
                    .setIp(ip)
                    .setPort(port)
                    .setHostName(this.hostName)
                    .setServiceName(serviceName)
                    .setVersions(getVersionsResponse())
                    .setBuildTime(buildTime)
                    .setIntegrations(integrations);

            return praefectusClient.register(entry);
        } else {
            return Mono.just("OK");
        }
    }

    public Mono<String> unregister() {
        if(serviceRegistrationEnabled) {
            System.out.println("Unregister: " + this.hostName);
            LOG.info("Unregistering Service with Tiberius Praefectus");
            return praefectusClient.unregister(this.hostName);
        } else {
            return Mono.just("OK");
        }
    }

    private VersionsResponse getVersionsResponse() {
        return new VersionsResponse()
                .setServiceVersion(versionProperties.getAppVersion())
                .setTiberiusCoreVersion(versionProperties.getTiberiusCoreVersion())
                .setErrorWebfluxStarterVersion(versionProperties.getErrorWebfluxStarterVersion())
                .setRequestWebfluxStarterVersion(versionProperties.getRequestWebfluxStarterVersion())
                .setJacksonWebfluxStarterVersion(versionProperties.getJacksonWebfluxStarterVersion())
                .setSpringBootVersion(versionProperties.getSpringBootVersion())
                .setSpringVersion(versionProperties.getSpringVersion())
                .setLog4jVersion(versionProperties.getLog4jVersion());
    }

    public Mono<Map<String, Map<String, String>>> getApplicationConfig() {
        Map<String, Map<String, String>> props = springEnv.getPropertySources()
                .stream()
                .filter(ps -> ps instanceof EnumerablePropertySource)
                .map(ps -> ((EnumerablePropertySource<?>) ps))
                .filter(ps -> !BLACKLISTED_PROPERTY_SOURCES.contains(ps.getName()))
                .map(ps -> {
                    Map<String, String> properties = Arrays.stream(ps.getPropertyNames())
                            .collect(Collectors.toMap(name -> name, name -> springEnv.getProperty(name, ""), (a, b) -> b));
                    return Pair.of(convertPropertySourceName(ps.getName()), properties);
                })
                .collect(Collectors.toMap(Pair::getLeft, Pair::getRight, (p1, p2) -> {
                    p1.putAll(p2);
                    return p1;
                }));
        // Specially handle for: tiberius.service.version as it is @build.version@
        props.getOrDefault("Versions", new HashMap<>()).put("tiberius.service.version", versionProperties.getAppVersion());

        // Hide sensitive data
        props.values().forEach(map -> {
            final List<String> overrides = new ArrayList<>();
            map.keySet().forEach(propertyKey -> {
                final String lcKey = propertyKey.toLowerCase();
                if(BLACKLISTED_PROP_KEYS.stream().anyMatch(lcKey::endsWith)) {
                    overrides.add(propertyKey);
                }
            });
            overrides.forEach(k -> map.put(k, "********************"));
        });

        return Mono.just(props);
    }

    private String convertPropertySourceName(String propertySourceName) {
        if(StringUtils.isEmpty(propertySourceName)) {
            return "Other";
        }
        if(propertySourceName.contains("apimarket4j")) {
            return "Api Market";
        }
        if(propertySourceName.contains("apigee4j")) {
            return "Apigee";
        }
        if(propertySourceName.contains("build-info")) {
            return "Build Info";
        }
        if(propertySourceName.contains("versions")) {
            return "Versions";
        }
        if(propertySourceName.contains("application")) {
            return "Application";
        }
        if(propertySourceName.contains("tiberius")) {
            return "Tiberius";
        }
        if(propertySourceName.contains("defaultProperties")) {
            return "Tiberius";
        }
        if(propertySourceName.startsWith("class path resource")) {
            Pattern pattern = Pattern.compile("^class path resource \\[(.*).properties]$");
            Matcher m = pattern.matcher(propertySourceName);
            if(m.matches() && StringUtils.isNotEmpty(m.group(1))) {
                return WordUtils.capitalize(m.group(1).replace("-", " "));
            }
        }
        return propertySourceName;
    }

    public Mono<Void> setLogLevel(String sLevel, String loggerName) {
        Level level = getLevelFromString(sLevel);
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);

        // Update level of all "child" loggers
        List<org.apache.logging.log4j.core.Logger> updatedLoggers = ctx.getLoggers()
                .stream()
                .filter(logger -> logger.getName().startsWith(loggerName))
                .collect(Collectors.toList());

        updatedLoggers.forEach(logger -> logger.setLevel(level));

        final int numberOfUpdatedLoggers = updatedLoggers.size();
        if(numberOfUpdatedLoggers == 0) {
            LOG.warn("No loggers updated");
            return Mono.empty();
        }

        final org.apache.logging.log4j.core.Logger firstLogger = updatedLoggers.get(0);
        if(numberOfUpdatedLoggers == 1) {
            LOG.info("Log level set to \033[0;35m\033[1m{}\033[0m for:\033[0;35m{}\033[0m", firstLogger.getLevel(), firstLogger.getName());
            return Mono.empty();
        }

        String loggerNames = updatedLoggers.stream().map(AbstractLogger::getName).collect(Collectors.joining("\n"));
        LOG.info("\nUpdated \033[0;35m\033[1m{}\033[0m loggers, level set to \033[0;35m\033[1m{}\033[0m for:\n\033[0;35m{}\033[0m", numberOfUpdatedLoggers, firstLogger.getLevel(), loggerNames);
        return Mono.empty();
    }

    /**
     * Returns true if message were logged otherwise false.
     */
    public Mono<Boolean> log(String sLevel, String loggerName, String message) {
        if(System.currentTimeMillis() - lastLogged < 1000) {
            // Logging spam protection five million
            throw new ForbiddenException(BaseErrors.TOO_MANY_REQUESTS, "Logging is on cool down");
        }
        this.lastLogged = System.currentTimeMillis();
        Level level = getLevelFromString(sLevel);

        final Logger logger = loggerName != null ? LoggerFactory.getLogger(loggerName) : LOG;

        final String colorfulMessage = String.format("\033[1m\033[0;35m%s\033[0m", message);

        if(level.equals(Level.TRACE) && logger.isTraceEnabled()) {
            logger.trace(colorfulMessage);
            return Mono.just(true);
        } else if(level.equals(Level.DEBUG) && logger.isDebugEnabled()) {
            logger.debug(colorfulMessage);
            return Mono.just(true);
        } else if(level.equals(Level.INFO) && logger.isInfoEnabled()) {
            logger.info(colorfulMessage);
            return Mono.just(true);
        } else if(level.equals(Level.WARN) && logger.isWarnEnabled()) {
            logger.warn(colorfulMessage);
            return Mono.just(true);
        } else if(level.equals(Level.ERROR) && logger.isErrorEnabled()) {
            logger.error(colorfulMessage);
            return Mono.just(true);
        } else {
            return Mono.just(false);
        }
    }

    private static Level getLevelFromString(String sLevel) {
        Level level = Level.getLevel(StringUtils.upperCase(sLevel));
        if(level == null || !LOGGABLE_LEVELS.contains(level)) {
            throw new BadRequestException(BaseErrors.INVALID_LOG_LEVEL, "Level " + sLevel + " is not a valid log level. Must be any of: [" + LOGGABLE_LEVELS + "]");
        }
        return level;
    }

    public Mono<String> readDependenciesFile() {
        return RequestProcessor.scheduleBlocking(() -> {
            String location = ResourceUtils.CLASSPATH_URL_PREFIX + "/bumper/dependencies.json";

            final Resource resource = RESOURCE_LOADER.getResource(location);
            if(resource.exists()) {
                try {
                    InputStreamReader isReader = new InputStreamReader(resource.getInputStream());
                    BufferedReader reader = new BufferedReader(isReader);
                    StringBuilder sb = new StringBuilder();
                    String str;
                    while((str = reader.readLine()) != null) {
                        sb.append(str);
                    }
                    return sb.toString();
                } catch(IOException e) {
                    throw new NotFoundException(BaseErrors.INVALID_DEPENDENCIES_FILE, "Could not read dependencies file", e);
                }
            } else {
                throw new NotFoundException(BaseErrors.NO_DEPENDENCIES_FILE, "Could not read dependencies file as it does not exist");
            }
        });
    }
}
