package com.teliacompany.tiberius.base.test.exception;

public class InvalidTiberiusApplicationRunning extends RuntimeException {
    public InvalidTiberiusApplicationRunning(String expectedAppName, String actualAppName, int port) {
        super("Invalid application running on port " + port + ". Expected application name to be \"" + expectedAppName + "\" but running app name is: " + actualAppName);
    }
}
