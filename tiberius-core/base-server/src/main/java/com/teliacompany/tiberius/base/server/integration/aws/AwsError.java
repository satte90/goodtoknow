package com.teliacompany.tiberius.base.server.integration.aws;

import com.teliacompany.webflux.error.api.ErrorEnum;
import org.springframework.http.HttpStatus;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.secretsmanager.model.DecryptionFailureException;
import software.amazon.awssdk.services.secretsmanager.model.InternalServiceErrorException;
import software.amazon.awssdk.services.secretsmanager.model.InvalidParameterException;
import software.amazon.awssdk.services.secretsmanager.model.InvalidRequestException;
import software.amazon.awssdk.services.secretsmanager.model.ResourceNotFoundException;
import software.amazon.awssdk.services.secretsmanager.model.SecretsManagerException;

import java.util.Arrays;

public enum AwsError implements ErrorEnum {
    RESOURCE_NOT_FOUND_EXCEPTION(ResourceNotFoundException.class, HttpStatus.NOT_FOUND, "We can't find the resource that you asked for"),
    INVALID_PARAMETER_EXCEPTION(InvalidParameterException.class, HttpStatus.BAD_REQUEST, "You provided an invalid value for a parameter."),
    INVALID_REQUEST_EXCEPTION(InvalidRequestException.class, HttpStatus.BAD_REQUEST, "You provided a parameter value that is not valid for the current state of the resource."),
    DECRYPTION_FAILURE_EXCEPTION(DecryptionFailureException.class, HttpStatus.UNAUTHORIZED, "Secrets Manager can't decrypt the protected secret text using the provided KMS key."),
    INTERNAL_SERVICE_ERROR_EXCEPTION(InternalServiceErrorException.class, HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred on the server side."),
    SDK_EXCEPTION(SdkException.class, HttpStatus.INTERNAL_SERVER_ERROR, "Base class for all exceptions that can be thrown by the SDK (both service and client). Can be used for catch all scenarios."),
    SDK_CLIENT_EXCEPTION(SdkClientException.class, HttpStatus.INTERNAL_SERVER_ERROR, "If any client side error occurs such as an IO related failure, failure to get credentials, etc."),
    SECRETS_MANAGER_EXCEPTION(SecretsManagerException.class, HttpStatus.INTERNAL_SERVER_ERROR, "Base class for all service exceptions. Unknown exceptions will be thrown as an instance of this type.");

    private final Class<? extends RuntimeException> awsException;
    private final HttpStatus httpStatus;
    private final String description;

    AwsError(Class<? extends RuntimeException> awsException, HttpStatus httpStatus, String description) {
        this.awsException = awsException;
        this.httpStatus = httpStatus;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public static AwsError fromAwsException(Throwable e) {
        return Arrays.stream(AwsError.values())
                .filter(awsError -> awsError.awsException.equals(e.getClass()))
                .findFirst()
                .orElse(INTERNAL_SERVICE_ERROR_EXCEPTION);
    }
}
