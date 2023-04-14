package com.teliacompany.tiberius.base.test.exception;

public class TiberiusKafkaBrokerInjectionException extends RuntimeException {
    public TiberiusKafkaBrokerInjectionException(Throwable e) {
        super("Could not inject Test Kafka Broker", e);
    }
}
