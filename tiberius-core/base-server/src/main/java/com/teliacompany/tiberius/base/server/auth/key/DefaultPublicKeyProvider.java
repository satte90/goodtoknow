package com.teliacompany.tiberius.base.server.auth.key;

import com.teliacompany.tiberius.base.server.api.BaseErrors;
import com.teliacompany.webflux.error.exception.client.UnauthorizedException;
import com.teliacompany.tiberius.base.server.integration.auth.TiberiusUserAuthClient;
import com.teliacompany.tiberius.user.auth.TiberiusAuthKeyUtils;
import com.teliacompany.tiberius.user.auth.api.v1.Base64Key;
import com.teliacompany.tiberius.user.auth.exception.TiberiusAuthKeyGenerationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Fetches public keys via http calls using authClient. Can be overridden by implementing PublicKeyProvider
 */
@Service
public class DefaultPublicKeyProvider implements PublicKeyProvider {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultPublicKeyProvider.class);
    private static final String JWT_VALIDATION_ERROR_MESSAGE = "Could not validate JWT, try logging out and back in again";

    private final TiberiusUserAuthClient authClient;

    private final Map<String, PublicKey> publicKeys = new HashMap<>();

    public DefaultPublicKeyProvider(TiberiusUserAuthClient authClient) {
        this.authClient = authClient;
    }

    @Override
    public Mono<PublicKey> getLatestPublicKey() {
        LOG.info("Getting latest public key and caching it");
        return authClient.getLatestKey()
                .map(this::convertAndSavePublicKey);
    }

    @Override
    public Mono<PublicKey> getPublicKey(String keyId) {
        return Optional.ofNullable(publicKeys.get(keyId))
                .map(Mono::just)
                .orElseGet(() -> authClient.getKey(keyId)
                        .map(this::convertAndSavePublicKey));
    }

    private PublicKey convertAndSavePublicKey(Base64Key base64Key) {
        try {
            PublicKey publicKey = TiberiusAuthKeyUtils.convertPublicKey(base64Key.getValue());
            publicKeys.put(base64Key.getId(), publicKey);
            return publicKey;
        } catch(TiberiusAuthKeyGenerationException e) {
            //Key we received for the id in JWT is invalid, something fishy going on? Or service is down?
            throw new UnauthorizedException(BaseErrors.JWT_VALIDATION_ERROR, JWT_VALIDATION_ERROR_MESSAGE);
        }
    }
}
