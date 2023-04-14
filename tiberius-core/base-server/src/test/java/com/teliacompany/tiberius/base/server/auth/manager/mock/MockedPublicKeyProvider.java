package com.teliacompany.tiberius.base.server.auth.manager.mock;

import com.teliacompany.tiberius.base.server.auth.key.PublicKeyProvider;
import reactor.core.publisher.Mono;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

public final class MockedPublicKeyProvider implements PublicKeyProvider {
    private Map<String, KeyPair> keys = new HashMap<>();
    private String latestKeyPairId = null;
    private KeyPair latestKeyPair = null;

    public MockedPublicKeyProvider(String keyId) {
        generatedKeyPair(keyId);
    }

    public void generatedKeyPair(String id) {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            keys.put(id, keyPair);
            latestKeyPair = keyPair;
            latestKeyPairId = id;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Mono<PublicKey> getLatestPublicKey() {
        return Mono.just(latestKeyPair.getPublic());
    }

    @Override
    public Mono<PublicKey> getPublicKey(String keyId) {
        return Mono.just(keys.get(keyId).getPublic());
    }

    public PrivateKey getLatestPrivateKey() {
        return latestKeyPair.getPrivate();
    }

    public String getLatestKeyPairId() {
        return latestKeyPairId;
    }
}
