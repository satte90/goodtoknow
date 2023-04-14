package com.teliacompany.tiberius.base.server.secrets.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import reactor.core.publisher.Mono;

import java.security.PublicKey;

public class DisabledSecretAgent implements SecretAgent {
    private static final Logger LOG = LoggerFactory.getLogger(DisabledSecretAgent.class);

    @Override
    public Mono<Boolean> fetchSecrets(@Nullable PublicKey notUsed) {
        LOG.info("Secret Agent: DISABLED");
        return Mono.empty();
    }

    @Override
    public Mono<Boolean> refreshSecrets() {
        return Mono.empty();
    }
}
