package com.teliacompany.tiberius.base.test.exception;

public class TiberiusWebTestClientInjectionException extends RuntimeException {
    public TiberiusWebTestClientInjectionException(Throwable e) {
        super("Could not inject WebTestClient", e);
    }
}
