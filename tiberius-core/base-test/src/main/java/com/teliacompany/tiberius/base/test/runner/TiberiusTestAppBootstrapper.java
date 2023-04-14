package com.teliacompany.tiberius.base.test.runner;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.teliacompany.tiberius.base.server.api.TestModeData;
import com.teliacompany.tiberius.base.test.TiberiusKafkaTestConsumer;
import com.teliacompany.tiberius.base.test.TiberiusTestConfig;
import com.teliacompany.tiberius.base.test.TiberiusWebTestClient;
import com.teliacompany.tiberius.base.test.client.ComponentTestClient;
import com.teliacompany.tiberius.base.test.client.WebTestClientStore;
import com.teliacompany.tiberius.base.test.exception.InvalidTiberiusApplicationRunning;
import com.teliacompany.tiberius.base.test.exception.TiberiusKafkaBrokerInjectionException;
import com.teliacompany.tiberius.base.test.exception.TiberiusSpringBootApplicationNotFound;
import com.teliacompany.tiberius.base.test.exception.TiberiusWebTestClientInjectionException;
import com.teliacompany.tiberius.base.test.kafka.ComponentTestKafkaConsumer;
import com.teliacompany.tiberius.base.test.logger.TestLoggerFactory;
import com.teliacompany.tiberius.base.test.mock.ApiMarketMock;
import com.teliacompany.tiberius.base.test.mock.ApigeeMock;
import com.teliacompany.tiberius.base.test.mock.SpockAuthMock;
import com.teliacompany.tiberius.base.utils.ClassNameUtils;
import com.teliacompany.tiberius.base.utils.MapInitializer;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.logging.log4j.Level;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.reactive.context.StandardReactiveWebEnvironment;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.teliacompany.tiberius.base.test.TiberiusTestConfig.AUTO_APP_NAME;

public final class TiberiusTestAppBootstrapper {
    private static final String DATABASE_NAME = "componenttest";
    private static TiberiusTestAppBootstrapper instance;

    private static final Logger LOG = TestLoggerFactory.getLogger(TiberiusTestAppBootstrapper.class);

    private static final String HASHTAG_LINE = "###################################";
    private static final String SPRING_APPLICATION_NAME = "spring.application.name";
    private static final String WIRE_MOCK_PORT_CONFIG_KEY = "wiremock.port";
    private static final String SERVER_PORT_CONFIG_KEY = "server.port";
    private static final String REGISTER_KEY = "tiberius.praefectus.register";
    private static final String REGISTER_HOST_NAME = "tiberius.praefectus.register.name";
    private static final String REGISTER_HOST_KEY = "tiberius.praefectus.register.host";
    private static final String REGISTER_PATH_KEY = "tiberius.praefectus.register.path";
    private static final String SECRETS_PROVIDER = "tiberius.secrets.provider";
    private static final String USER_AUTH_ENABLED_KEY = "tiberius.user.auth.enabled";
    private static final String METRICS_PREFIX_CONFIG_KEY = "metrics.prefix";
    private static final String API_MARKET_ENDPOINT_CONFIG_KEY = "apiMarket.refreshUrl";
    private static final String API_MARKET_KEY_CONFIG_KEY = "apiMarket.key";
    private static final String API_MARKET_SECRET_CONFIG_KEY = "apiMarket.secret";
    private static final String MAX_PAYLOAD_LOGGING_LENGTH = "logging.maxPayloadLoggingLength";
    private static final String MAX_PAYLOAD_ENC_LOGGING_LENGTH = "logging.maxEncodedPayloadLoggingLength";
    private static final String LOGGING_IGNORE_PATHS = "logging.ignorePaths";
    private static final String LOGGING_ENCODE_PAYLOAD = "logging.encodePayload";
    private static final String LOGGING_ENCODE_HEADERS = "logging.encodeHeaders";
    private static final String LOGGING_LOG_AS_OBJECT_MESSAGE = "logging.logAsObjectMessage";
    private static final String LOGGING_CONFIG = "logging.config";
    private static final String TIBERIUS_CORE_VERSION_KEY = "tiberius.core.version";
    private static final String WEBFLUX_STARTER_VERSION_KEY = "tiberius.dependency.request.webflux.starter.version";
    private static final String ERROR_STARTER_VERSION_KEY = "tiberius.dependency.error.webflux.starter.version";
    private static final String TIBERIUS_MONGODB_USER_KEY = "tiberius.mongodb.user";
    private static final String TIBERIUS_MONGODB_PSW_KEY = "tiberius.mongodb.password";
    private static final String TIBERIUS_MONGODB_HOST_KEY = "tiberius.mongodb.host";
    private static final String TIBERIUS_MONGODB_PORT_KEY = "tiberius.mongodb.port";
    private static final String TIBERIUS_MONGODB_DATABASE_KEY = "tiberius.mongodb.database";
    private static final String C2BCACHE_APPLICATION_ID_KEY = "c2bcache.applicationid";
    private static final String C2BCACHE_PORTAL_ID_KEY = "c2bcache.portalid";
    private static final String TIBERIUS_VAULT_ENABLED = "tiberius.vault.enabled";
    private static final String TIBERIUS_API_KEY = "tiberius.api.key";
    private static final String TIBERIUS_API_SECRET = "tiberius.api.key";

    private static final String HAZELCAST_PASSWORD_CONFIG_KEY = "hazelcast.password";
    private static final String HAZELCAST_NAMESPACE_CONFIG_KEY = "hazelcast.namespace";
    private static final String HAZELCAST_SERVICE_NAME_CONFIG_KEY = "hazelcast.serviceName";

    private static final String TSE_HOST = "com.teliacompany.tse.host";
    private static final String AUG_HOST = "com.teliacompany.augustus.host";
    private static final String TIBERIUS_BASE_PATH = "tiberius.base.path";
    private static final String TIBERIUS_USER_AUTH_BASE_PATH = "tiberius.user.auth.base.path";
    private static final String TIBERIUS_USER_AUTH_HOST = "tiberius.user.auth.host";
    private static final String TIBERIUS_SLACK_DEVOPS_ENABLED = "tiberius.slack.devops.enabled";
    private static final String TIBERIUS_SLACK_DEVOPS_DEFAULT_ENDPOINT = "tiberius.slack.devops.default.endpoint";

    private static final String KAFKA_ENABLED = "kafka.enabled";
    private static final String KAFKA_BOOTSTRAP_SERVERS = "kafka.bootstrap.servers";
    private static final String KAFKA_SECURITY_PROTOCOL = "kafka.security.protocol";
    private static final String KAFKA_SASL_MECHANISM = "kafka.sasl.mechanism";
    private static final String KAFKA_SSL_ENDPOINT_IDENTIFICATION_ALGORITHM = "kafka.ssl.endpoint.identification.algorithm";

    private SpringApplication app;

    // Config - Can be set withConsumer config annotation
    private int port;
    private int wiremockPort;
    private int mongodbPort;
    private int kafkaPort;
    private boolean mongodbEnabled;
    private boolean kafkaEnabled;
    private String testDateTime;
    private String testConfiguredAppName;
    private String appName;
    private String basePath;

    // State
    private boolean appRunning = false;
    private boolean wireMockRunning = false;
    private boolean mongoDbRunning = false;
    private boolean kafkaRunning = false;
    private boolean testModeEnabled = false;
    private boolean appNameValidated = false;

    private static WireMockServer mockHttpServer;
    private ComponentTestClient webTestClient;
    private WebClient webClient;
    private List<Field> webTestClientFields = new ArrayList<>();
    private List<Field> kafkaConsumerFields = new ArrayList<>();
    private MongoServer mongoServer;
    private EmbeddedKafkaBroker kafkaBroker;

    private String currentTestClassName;
    private String currentTestMethodName;
    private Level logLevel;
    private String[] kafkaTopics;

    static TiberiusTestAppBootstrapper instance(Class<?> testClass) {
        if(instance == null) {
            instance = new TiberiusTestAppBootstrapper(testClass);
        } else {
            instance.findAnnotatedWebTestClientFields(testClass);
            instance.findAnnotatedKafkaConsumerFields(testClass);
        }
        return instance;
    }

    static TiberiusTestAppBootstrapper getCurrentInstance() {
        return instance;
    }

    private TiberiusTestAppBootstrapper(Class<?> testClass) {
        logWithHashTags("Initializing Bootstrapper");
        setUpConfig(testClass);
        setUpWireMock();
        findAnnotatedWebTestClientFields(testClass);
        findAnnotatedKafkaConsumerFields(testClass);
        setUpMongo();
        setUpKafka();
        setUpSpringBootApp();
        setUpWebTestClient();
        setUpWebClient();
        Runtime.getRuntime().addShutdownHook(new Thread(this::testsEnded));
        LOG.info("Bootstrapper initialized");
    }

    private void setUpConfig(Class<?> testClass) {
        Optional<TiberiusTestConfig> oAnnotation = findConfigAnnotation(testClass);
        if(oAnnotation.isEmpty()) {
            warnLog("No TiberiusTestConfig annotation found on test class. Using default values.");
        }

        testConfiguredAppName = oAnnotation.map(TiberiusTestConfig::applicationName).orElse(AUTO_APP_NAME);
        basePath = oAnnotation.map(TiberiusTestConfig::serverBasePath).orElse(TiberiusTestConfig.AUTO_BASE_PATH);
        port = oAnnotation.map(TiberiusTestConfig::port).orElse(TiberiusTestConfig.DEFAULT_PORT);
        wiremockPort = oAnnotation.map(TiberiusTestConfig::wiremockPort).orElse(TiberiusTestConfig.DEFAULT_WIREMOCK_PORT);
        testDateTime = oAnnotation.map(TiberiusTestConfig::testDate).orElse(TiberiusTestConfig.DEFAULT_TEST_DATE);
        mongodbEnabled = oAnnotation.map(TiberiusTestConfig::mongodbEnabled).orElse(TiberiusTestConfig.DEFAULT_MONGODB_ENABLED);
        mongodbPort = oAnnotation.map(TiberiusTestConfig::mongodbPort).orElse(TiberiusTestConfig.DEFAULT_MONGODB_PORT);
        kafkaPort = oAnnotation.map(TiberiusTestConfig::kafkaPort).orElse(TiberiusTestConfig.DEFAULT_MONGODB_PORT);
        kafkaEnabled = oAnnotation.map(TiberiusTestConfig::kafkaEnabled).orElse(TiberiusTestConfig.DEFAULT_KAFKA_ENABLED);
        kafkaTopics = oAnnotation.map(TiberiusTestConfig::kafkaTopics).orElse(new String[]{TiberiusTestConfig.DEFAULT_KAFKA_TOPICS});
        logLevel = Level.getLevel(oAnnotation.map(TiberiusTestConfig::logLevel).orElse(TiberiusTestConfig.DEFAULT_LOG_LEVEL).toUpperCase(Locale.ROOT));

        infoLog("\n\nTiberius Test Config resolved to:\nApp Port: {}\nWireMock Port: {}\nMongoDb Port: {}\nKafka Port: {}\n", port, wiremockPort, mongodbPort, kafkaPort);
    }

    private static void infoLog(String s, Object... vars) {
        String s2 = s.replace("{}", "%s");
        Object[] args = Arrays.stream(vars).map(Object::toString).toArray(String[]::new);
        final String format = String.format(s2, args);
        LOG.info(format);
    }

    private static void warnLog(String s, Object... vars) {
        String s2 = s.replace("{}", "%s");
        Object[] args = Arrays.stream(vars).map(Object::toString).toArray(String[]::new);
        final String format = String.format(s2, args);
        LOG.warning(format);
    }

    private Optional<TiberiusTestConfig> findConfigAnnotation(Class<?> testClass) {
        while(testClass != null) {
            TiberiusTestConfig annotation = testClass.getDeclaredAnnotation(TiberiusTestConfig.class);
            if(annotation != null) {
                return Optional.of(annotation);
            }
            testClass = testClass.getSuperclass();
        }
        return Optional.empty();
    }

    private void setUpWireMock() {
        infoLog("Setup wiremock...");
        infoLog("Looking for running wiremock on port {}", wiremockPort);
        boolean wiremockDetected = pingLocalhost(wiremockPort);
        if(wiremockDetected) {
            infoLog("Found running WireMock! It will be used for tests.");
            wireMockRunning = true;
        } else {
            infoLog("No running wiremock found. Embedded WireMock will be started.");
            WireMockConfiguration options = WireMockConfiguration.options()
                    .port(wiremockPort)
                    .extensions(new ResponseTemplateTransformer(false));
            mockHttpServer = new WireMockServer(options);
        }
    }

    private void setUpMongo() {
        if(mongodbEnabled) {
            infoLog("Setup mongoDb...");
            infoLog("Looking for running mongodb on port {}", mongodbPort);
            boolean dbDetected = pingLocalhost(mongodbPort);
            if(dbDetected) {
                infoLog("Found running MongoDb! It will be used for tests.");
                mongoDbRunning = true;
            } else {
                infoLog("No running mongo found. Embedded in memory mongodb will be started.");
                mongoServer = new MongoServer(new MemoryBackend());
            }
        }
    }

    private void setUpKafka() {
        if(kafkaEnabled) {
            infoLog("Setup embedded kafka broker...");
            infoLog("Looking for running kafka on port {}", kafkaPort);
            boolean kafkaBrokerDetected = pingLocalhost(kafkaPort);
            if(kafkaBrokerDetected) {
                infoLog("Found running Kafka Broker! It will be used for tests.");
                kafkaRunning = true;
            } else {
                infoLog("No running kafka broker found. Embedded in kafka broker will be started.");
                infoLog("Topics: " + Arrays.toString(kafkaTopics));
                kafkaBroker = new EmbeddedKafkaBroker(1, false, kafkaTopics)
                        .zkPort(2888)
                        .kafkaPorts(kafkaPort)
                        .zkConnectionTimeout(EmbeddedKafkaBroker.DEFAULT_ZK_CONNECTION_TIMEOUT)
                        .zkSessionTimeout(EmbeddedKafkaBroker.DEFAULT_ZK_SESSION_TIMEOUT);

                final Map<String, String> properties = Map.of(
                        "listeners", "PLAINTEXT://localhost:" + kafkaPort,
                        "port", String.valueOf(kafkaPort));
                kafkaBroker.brokerProperties(properties);
            }
        }
    }

    private void setUpWebTestClient() {
        infoLog("Creating web test client...");
        this.webTestClient = new ComponentTestClient(
                WebTestClient.bindToServer()
                        .baseUrl("http://localhost:" + port + "/" + basePath)
                        //Disable the DataBufferLimit (seems to be introduced in spring boot 2.2.2) See https://stackoverflow.com/a/59506485
                        .exchangeStrategies(ExchangeStrategies.builder()
                                .codecs(configurer -> configurer.defaultCodecs()
                                        .maxInMemorySize(-1))
                                .build())
                        .build()
        );
        WebTestClientStore.registerWebTestClient(this.webTestClient);
    }

    private void setUpWebClient() {
        infoLog("Creating web test client...");
        final String baseUrl = "http://localhost:" + port + "/" + basePath;
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("REMOTE_USER", "test")
                .defaultHeader("SCUSERID", "test")
                .defaultHeader("x-tcad", "test")
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .build();
        infoLog("Web client configured for {}", baseUrl);

    }

    private void findAnnotatedWebTestClientFields(Class<?> testClass) {
        infoLog("Finding {} annotated field(s) in test...", TiberiusWebTestClient.class.getName());

        // Add annotated @WebTestClients as well (for backwards compatibility)
        webTestClientFields = FieldUtils.getFieldsListWithAnnotation(testClass, TiberiusWebTestClient.class)
                .stream()
                .filter(field -> field.getType().equals(ComponentTestClient.class) || field.getType().equals(WebTestClient.class))
                .collect(Collectors.toList());

        webTestClientFields.forEach(field -> field.setAccessible(true));

        infoLog("Found {} web test client field(s)", webTestClientFields.size());
    }

    private void findAnnotatedKafkaConsumerFields(Class<?> testClass) {
        if(kafkaEnabled) {
            infoLog("Finding {} annotated field(s) in test...", TiberiusKafkaTestConsumer.class.getName());

            // Add annotated @WebTestClients as well (for backwards compatibility)
            kafkaConsumerFields = FieldUtils.getFieldsListWithAnnotation(testClass, TiberiusKafkaTestConsumer.class)
                    .stream()
                    .filter(field -> field.getType().equals(ComponentTestKafkaConsumer.class))
                    .collect(Collectors.toList());

            kafkaConsumerFields.forEach(field -> field.setAccessible(true));

            infoLog("Found {} kafka consumer field(s)", kafkaConsumerFields.size());
        }
    }

    private void setUpSpringBootApp() {
        infoLog("Setting up spring boot app...");

        Class<?> springBootClass = findTiberiusApplicationClass();
        infoLog("Spring boot class found: {}", springBootClass);

        appName = testConfiguredAppName.equals(AUTO_APP_NAME) ? ClassNameUtils.simpleNameToKebabCase(springBootClass) : testConfiguredAppName;
        if(TiberiusTestConfig.AUTO_BASE_PATH.equals(basePath)) {
            basePath = appName.replace("-", "/") + "/";
        } else {
            basePath = StringUtils.removeEnd(basePath, "/") + "/";
        }

        boolean localServerRunning = pingLocalhost(port);
        if(localServerRunning) {
            infoLog("Local server running on {}. Tests will be running against it", port);
            appRunning = true;
        } else {
            infoLog("Local spring boot server not running on {}, setting up embedded app...", port);
            app = new SpringApplication(springBootClass);
            Map<String, Object> properties = getProperties();
            PropertySource<?> ps = new MapPropertySource("tiberius-componenttest-bootstrapped", properties);
            ConfigurableEnvironment env = new StandardReactiveWebEnvironment();
            env.getPropertySources().addLast(ps);
            app.setEnvironment(env);
            infoLog("Spring boot app set up");
        }
    }

    private static Class<?> findTiberiusApplicationClass() {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(SpringBootApplication.class));

        BeanDefinition bd = scanner.findCandidateComponents("com.teliacompany.tiberius")
                .stream()
                .findFirst()
                .orElseThrow(() -> {
                    LOG.severe("Could not find Spring boot application. It must be in package: \"com.teliacompany.tiberius.*\" " +
                            "and annotated with @SpringBootApplication or @TiberiusApplication");
                    return new TiberiusSpringBootApplicationNotFound();
                });

        try {
            return Class.forName(bd.getBeanClassName());
        } catch(ClassNotFoundException e) {
            throw new TiberiusSpringBootApplicationNotFound();
        }
    }

    private Map<String, Object> getProperties() {
        final String ignoredPathString = Stream.of("methods", "status/ping", "testsupport", "registry", "devops/log", "devops/name", "ldap/mock")
                .map(s -> "/" + basePath + s)
                .collect(Collectors.joining(","));
        final Map<String, Object> properties = MapInitializer.hashMapWithEntries(
                MapInitializer.Entry.of(SPRING_APPLICATION_NAME, appName),
                MapInitializer.Entry.of(WIRE_MOCK_PORT_CONFIG_KEY, wiremockPort),
                MapInitializer.Entry.of(SERVER_PORT_CONFIG_KEY, port),
                MapInitializer.Entry.of(REGISTER_KEY, false),
                MapInitializer.Entry.of(REGISTER_HOST_NAME, "MockPraefectus"),
                MapInitializer.Entry.of(REGISTER_HOST_KEY, "http://localhost:" + wiremockPort),
                MapInitializer.Entry.of(REGISTER_PATH_KEY, ""),
                MapInitializer.Entry.of(SECRETS_PROVIDER, "none"),
                MapInitializer.Entry.of(USER_AUTH_ENABLED_KEY, false),
                MapInitializer.Entry.of(API_MARKET_ENDPOINT_CONFIG_KEY, "http://localhost:" + wiremockPort + "/token"),
                MapInitializer.Entry.of(API_MARKET_KEY_CONFIG_KEY, "testkey"),
                MapInitializer.Entry.of(API_MARKET_SECRET_CONFIG_KEY, "testsecret"),
                MapInitializer.Entry.of(METRICS_PREFIX_CONFIG_KEY, "aug"),
                MapInitializer.Entry.of(MAX_PAYLOAD_LOGGING_LENGTH, 1000),
                MapInitializer.Entry.of(MAX_PAYLOAD_ENC_LOGGING_LENGTH, 1000),
                MapInitializer.Entry.of(LOGGING_IGNORE_PATHS, ignoredPathString),
                MapInitializer.Entry.of(LOGGING_CONFIG, "classpath:log4j2-local.xml"),
                MapInitializer.Entry.of(LOGGING_LOG_AS_OBJECT_MESSAGE, false),
                MapInitializer.Entry.of(LOGGING_ENCODE_HEADERS, false),
                MapInitializer.Entry.of(LOGGING_ENCODE_PAYLOAD, false),
                MapInitializer.Entry.of(TIBERIUS_CORE_VERSION_KEY, "test"),
                MapInitializer.Entry.of(WEBFLUX_STARTER_VERSION_KEY, "test"),
                MapInitializer.Entry.of(ERROR_STARTER_VERSION_KEY, "test"),
                MapInitializer.Entry.of(C2BCACHE_APPLICATION_ID_KEY, appName),
                MapInitializer.Entry.of(C2BCACHE_PORTAL_ID_KEY, "OurTelia"),
                MapInitializer.Entry.of(TIBERIUS_VAULT_ENABLED, false),
                MapInitializer.Entry.of(TIBERIUS_API_KEY, "test"),
                MapInitializer.Entry.of(TIBERIUS_API_SECRET, "test"),
                MapInitializer.Entry.of(TIBERIUS_MONGODB_DATABASE_KEY, DATABASE_NAME),
                MapInitializer.Entry.of(TIBERIUS_MONGODB_HOST_KEY, "localhost"),
                MapInitializer.Entry.of(TIBERIUS_MONGODB_PORT_KEY, String.valueOf(mongodbPort)),
                MapInitializer.Entry.of(TIBERIUS_MONGODB_USER_KEY, ""),
                MapInitializer.Entry.of(TIBERIUS_MONGODB_PSW_KEY, ""),
                MapInitializer.Entry.of(HAZELCAST_SERVICE_NAME_CONFIG_KEY, "appName"),
                MapInitializer.Entry.of(HAZELCAST_PASSWORD_CONFIG_KEY, "testlösenordet"),
                MapInitializer.Entry.of(HAZELCAST_NAMESPACE_CONFIG_KEY, "tse"),
                MapInitializer.Entry.of(TSE_HOST, "http://localhost:${wiremock.port}"),
                MapInitializer.Entry.of(AUG_HOST, "http://localhost:${wiremock.port}"),
                MapInitializer.Entry.of(TIBERIUS_BASE_PATH, "/tiberius"),
                MapInitializer.Entry.of(TIBERIUS_USER_AUTH_BASE_PATH, "${tiberius.base.path}/user/auth/"),
                MapInitializer.Entry.of(TIBERIUS_USER_AUTH_HOST, "http://localhost:${wiremock.port}"),
                MapInitializer.Entry.of(TIBERIUS_SLACK_DEVOPS_ENABLED, "false"),
                MapInitializer.Entry.of(TIBERIUS_SLACK_DEVOPS_DEFAULT_ENDPOINT, "none"),
                MapInitializer.Entry.of(KAFKA_ENABLED, kafkaEnabled),
                MapInitializer.Entry.of(KAFKA_BOOTSTRAP_SERVERS, "127.0.0.1:9092"),
                MapInitializer.Entry.of(KAFKA_SECURITY_PROTOCOL, "PLAINTEXT"),
                MapInitializer.Entry.of(KAFKA_SASL_MECHANISM, "GSSAPI"),
                MapInitializer.Entry.of(KAFKA_SSL_ENDPOINT_IDENTIFICATION_ALGORITHM, "https")
        );

        final List<String> propList = properties.entrySet()
                .stream()
                .map(entry -> entry.getKey() + " = " + entry.getValue())
                .sorted()
                .collect(Collectors.toList());
        final String propString = String.join("\n", propList);
        infoLog("\n\nSetting server properties:\n{}\n", propString);

        return properties;
    }

    /**
     * Injects the webTestClient into the @TiberiusWebTestClient annotated fields in the testClass
     */
    static void injectWebTestClient(Object instance) {
        if(TiberiusTestAppBootstrapper.instance != null) {

            infoLog("Injecting web test client on @TiberiusWebTestClient annotated fields of type WebTestClient");
            TiberiusTestAppBootstrapper.instance.webTestClientFields.forEach(field -> {
                try {
                    TiberiusWebTestClient annotation = field.getAnnotation(TiberiusWebTestClient.class);
                    WebTestClient webClientForTest = TiberiusTestAppBootstrapper.instance.webTestClient.mutate()
                            .defaultHeader("telia-tcad", annotation.tcadHeader())
                            .defaultHeader("x-tcad", annotation.tcadHeader())
                            .defaultHeader("REMOTE_USER", annotation.tcadHeader())
                            .defaultHeader("SCUSERID", annotation.tcwssIdHeader())
                            .defaultHeader("Accept", annotation.defaultAcceptHeader())
                            .defaultHeader("ContentType", annotation.defaultContentTypeHeader())
                            .defaultHeader(annotation.defaultHeaderKey(), annotation.defaultHeaderValue())
                            .responseTimeout(Duration.of(annotation.timeoutMinutes(), ChronoUnit.MINUTES))
                            .build();
                    field.set(instance, new ComponentTestClient(webClientForTest));
                } catch(Exception e) {
                    throw new TiberiusWebTestClientInjectionException(e);
                }
            });
        }
    }

    /**
     * Injects the kafkaBroker into the @TiberiusWebTestClient annotated fields in the testClass
     */
    static ComponentTestKafkaConsumer injectKafkaConsumer(Object instance) {
        final TiberiusTestAppBootstrapper bootstrapperInstance = TiberiusTestAppBootstrapper.instance;
        if(bootstrapperInstance != null && bootstrapperInstance.kafkaBroker != null) {
            infoLog("Injecting kafka broker on @TiberiusKafkaTestConsumer annotated fields of type ComponentTestKafkaConsumer");
            final EmbeddedKafkaBroker kafkaBroker = bootstrapperInstance.kafkaBroker;

            //Get default props from util class and set key deserializer to String. For now this is enough, maybe in future we may need other deserializers
            Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("testConsumer", "false", kafkaBroker);
            consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

            ComponentTestKafkaConsumer kafkaConsumer = new ComponentTestKafkaConsumer(kafkaBroker);
            kafkaBroker.consumeFromAllEmbeddedTopics(kafkaConsumer);

            bootstrapperInstance.kafkaConsumerFields.forEach(field -> {
                try {
                    field.set(instance, kafkaConsumer);
                } catch(Exception e) {
                    throw new TiberiusKafkaBrokerInjectionException(e);
                }
            });
            return kafkaConsumer;
        }
        return null;
    }

    private static boolean pingLocalhost(int port) {
        LOG.fine("Pinging localhost on port " + port + "...");
        try(Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("localhost", port), 5000);
            return true;
        } catch(IOException e) {
            return false; // Either timeout or unreachable or failed DNS lookup.
        }
    }

    public void startMongoIfNotRunningAndEnabled() {
        if(!mongoDbRunning && mongodbEnabled) {
            mongoDbRunning = true;
            mongoServer.bind("localhost", mongodbPort);
        }
    }

    public void startKafkaIfNotRunningAndEnabled() {
        if(!kafkaRunning && kafkaEnabled) {
            logWithHashTags("Starting Kafka...");
            kafkaRunning = true;
            kafkaBroker.afterPropertiesSet();
        }
    }

    void startWiremockIfNotRunning() {
        if(!wireMockRunning) {
            wireMockRunning = true;
            logWithHashTags("Starting WireMock...");
            mockHttpServer.addMockServiceRequestListener(WireMockRequestListener::requestReceived);
            mockHttpServer.start();
            infoLog("Wiremock started on {}", mockHttpServer.port());
        }

        //Always do this
        WireMock.configureFor("localhost", wiremockPort);
    }

    void startAppIfNotRunning() {
        if(!appRunning) {
            logWithHashTags("Starting spring boot app...");
            ConfigurableApplicationContext context = app.run();

            appRunning = true;
            infoLog("Spring boot app started on {}", context.getEnvironment().getProperty(SERVER_PORT_CONFIG_KEY));
        }
    }

    public void validateApplicationName() {
        if(!appNameValidated) {
            appNameValidated = true;
            infoLog("Validating that running app on port {} is called {}...", port, appName);
            String runningAppName = webClient.get()
                    .uri("devops/name")
                    .retrieve()
                    .bodyToMono(String.class)
                    .onErrorResume(e -> {
                        LOG.severe("Could not get AppName, defaulting to Unknown: \n" + e.getMessage());
                        return Mono.just("Unknown");
                    })
                    .block();

            if(!Objects.equals(runningAppName, appName)) {
                throw new InvalidTiberiusApplicationRunning(appName, runningAppName, port);
            }
            infoLog("Application name confirmed as: \"{}\"", runningAppName);
        }
    }

    /**
     * Call enable test mode endpoint. If the server was started with local flag this will make sure WebClients points to wirelmock
     */
    void enableTestMode() {
        if(!testModeEnabled) {
            this.testModeEnabled = true;
            TestModeData testData = new TestModeData();
            testData.setWiremockPort(wiremockPort);
            if(StringUtils.isNotEmpty(testDateTime)) {
                testData.setTimestamp(Instant.parse(testDateTime).toEpochMilli());
            }
            webClient.post()
                    .uri("testsupport/testmode/enable")
                    .body(BodyInserters.fromValue(testData))
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();

            if(logLevel != Level.INFO) {
                webClient.put()
                        .uri("devops/log/level/" + logLevel.name() + "?loggerName=" + getLoggerName())
                        .retrieve()
                        .bodyToMono(Void.class)
                        .block();
            }
        }
    }

    /**
     * Call the enable test mode endpoint. If the server was started with local flag this will make sure WebClients points to wirelmock
     */
    void disableTestMode() {
        if(testModeEnabled) {
            testModeEnabled = false;
            webClient.get()
                    .uri("testsupport/testmode/disable")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if(logLevel != Level.INFO) {
                webClient.put()
                        .uri("devops/log/level/" + logLevel.name() + "?loggerName=" + getLoggerName())
                        .retrieve()
                        .bodyToMono(Void.class)
                        .block();
            }
        }
    }

    void mockApiMarketAuthentication() {
        ApiMarketMock.mockAuthentication();
    }

    void mockApigeeAuthentication() {
        ApigeeMock.mockAuthentication();
    }

    void mockSpockAuthentication() {
        SpockAuthMock.mockAuthentication();
    }

    private void testsEnded() {
        disableTestMode();
        infoLog("Tests ended");
    }

    private void logWithHashTags(String message) {
        infoLog("\n{}\n  {}\n{}", HASHTAG_LINE, message, HASHTAG_LINE);
    }

    private String getLoggerName() {
        return "com.teliacompany." + appName.replace("-", ".");
    }

    public void clearMongo() {
        if(mongodbEnabled) {
            try(MongoClient client = MongoClients.create("mongodb://localhost:" + mongodbPort)) {
                Mono.from(client.getDatabase(DATABASE_NAME).drop())
                        .block();
            }
        }
    }

    public void setAndLogTestMethod(String className, String name) {
        this.currentTestClassName = className;
        this.currentTestMethodName = name;
        webClient.get()
                .uri("testsupport/starttest/" + currentTestClassName + "." + name)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public String getCurrentTestClassName() {
        return currentTestClassName;
    }

    public String getCurrentTestMethodName() {
        return currentTestMethodName;
    }

    private static TiberiusKafkaTestConsumer getTiberiusKafkaTestBroker(Field field) {
        try {
            return field.getAnnotation(TiberiusKafkaTestConsumer.class);
        } catch(Exception e) {
            throw new TiberiusKafkaBrokerInjectionException(e);
        }
    }
}
