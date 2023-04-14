package com.teliacompany.tiberius.crypto.exception;

public class TiberiusCryptoException extends RuntimeException {
    public TiberiusCryptoException(String message, Exception e) {
        super(message, e);
    }
}
