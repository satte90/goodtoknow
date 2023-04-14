package com.teliacompany.tiberius.base.server.exception;

public class MissingTiberiusAnnotationException extends RuntimeException {
    public MissingTiberiusAnnotationException() {
        super("Missing TiberiusApplication annotation");
    }
}
