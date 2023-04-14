package com.teliacompany.tiberius.base.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TiberiusTestConfig {
    int DEFAULT_PORT = 8080;
    int DEFAULT_WIREMOCK_PORT = 8089;
    int DEFAULT_MONGODB_PORT = 27018;
    int DEFAULT_KAFKA_PORT = 9092;
    String DEFAULT_TEST_DATE = "";
    String DEFAULT_APPROVALS_BASE_PATH = "approvals";
    String DEFAULT_LOG_LEVEL = "INFO";

    boolean DEFAULT_MONGODB_ENABLED = false;
    boolean DEFAULT_KAFKA_ENABLED = false;

    String DEFAULT_KAFKA_TOPICS = "test_topic";

    /**
     * Automatically determine base path based on application name
     */
    String AUTO_BASE_PATH = "auto";

    /**
     * Automatically calculate application name from application main class
     */
    String AUTO_APP_NAME = "auto";


    int port() default DEFAULT_PORT;

    int wiremockPort() default DEFAULT_WIREMOCK_PORT;

    int mongodbPort() default DEFAULT_MONGODB_PORT;

    int kafkaPort() default DEFAULT_KAFKA_PORT;

    String testDate() default DEFAULT_TEST_DATE;

    boolean mongodbEnabled() default DEFAULT_MONGODB_ENABLED;

    boolean kafkaEnabled() default DEFAULT_KAFKA_ENABLED;

    String serverBasePath() default AUTO_BASE_PATH;

    String applicationName() default AUTO_APP_NAME;

    String approvalsBasePath() default DEFAULT_APPROVALS_BASE_PATH;

    String logLevel() default DEFAULT_LOG_LEVEL;

    String[] kafkaTopics() default DEFAULT_KAFKA_TOPICS;

}
