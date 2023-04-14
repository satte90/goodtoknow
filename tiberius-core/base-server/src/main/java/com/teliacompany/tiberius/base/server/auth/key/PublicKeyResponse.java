package com.teliacompany.tiberius.base.server.auth.key;

import java.security.PublicKey;

public class PublicKeyResponse {
    private final PublicKey publicKey;
    private final boolean success;

    private PublicKeyResponse(PublicKey publicKey, boolean success) {
        this.publicKey = publicKey;
        this.success = success;
    }

    public static PublicKeyResponse success(PublicKey publicKey) {
        return new PublicKeyResponse(publicKey, true);
    }

    public static PublicKeyResponse disabled() {
        return new PublicKeyResponse(null, true);
    }

    public static PublicKeyResponse failure() {
        return new PublicKeyResponse(null, false);
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public boolean isSuccess() {
        return success;
    }
}
