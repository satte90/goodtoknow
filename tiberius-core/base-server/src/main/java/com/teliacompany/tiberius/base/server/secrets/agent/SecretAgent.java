package com.teliacompany.tiberius.base.server.secrets.agent;

import org.springframework.lang.Nullable;
import reactor.core.publisher.Mono;

import java.security.PublicKey;

public interface SecretAgent {
    Mono<Boolean> fetchSecrets(@Nullable PublicKey key);

    Mono<Boolean> refreshSecrets();
}
