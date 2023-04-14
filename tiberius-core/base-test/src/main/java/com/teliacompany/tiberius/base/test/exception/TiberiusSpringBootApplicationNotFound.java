package com.teliacompany.tiberius.base.test.exception;

public class TiberiusSpringBootApplicationNotFound extends RuntimeException {
    public TiberiusSpringBootApplicationNotFound() {
        super("Spring boot application class not found on classpath");
    }
}
