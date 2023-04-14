package com.teliacompany.tiberius.base.server.api;

import com.teliacompany.webflux.error.api.ErrorEnum;

public enum BaseErrors implements ErrorEnum {
    STARTUP_ERROR,
    MISSING_ENVIRONMENT_VARIABLE,
    JWT_VALIDATION_ERROR,
    TOO_MANY_REQUESTS,
    INVALID_LOG_LEVEL,
    INVALID_DEPENDENCIES_FILE,
    NO_DEPENDENCIES_FILE,
    AUTHENTICATION_FAILED
}
