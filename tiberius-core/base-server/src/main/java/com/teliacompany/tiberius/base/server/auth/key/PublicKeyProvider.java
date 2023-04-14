package com.teliacompany.tiberius.base.server.auth.key;

import com.teliacompany.tiberius.base.server.api.BaseErrors;
import com.teliacompany.webflux.error.exception.server.InternalServerErrorException;
import reactor.core.publisher.Mono;

import java.security.PublicKey;
import java.util.List;

/**
 * Implement to provide a custom PublicKeyProvider for TiberiusAuthenticationManager
 */
public interface PublicKeyProvider {

    Mono<PublicKey> getLatestPublicKey();

    Mono<PublicKey> getPublicKey(String keyId);

    static PublicKeyProvider getPrioritizedKeyProvider(List<PublicKeyProvider> publicKeyProviders) {
        if(publicKeyProviders.size() > 2) {
            throw new InternalServerErrorException(BaseErrors.STARTUP_ERROR, "To many PublicKeyProviders found, only one provider allowed on classpath excluding the default provider.");
        }

        PublicKeyProvider defaultPublicKeyProvider = publicKeyProviders.stream()
                .filter(kp -> kp instanceof DefaultPublicKeyProvider)
                .findFirst()
                .orElseThrow(() -> new InternalServerErrorException("DefaultPublicKeyProvider bean not present!"));

        return publicKeyProviders.stream()
                .filter(kp -> !(kp instanceof DefaultPublicKeyProvider))
                .findFirst()
                .orElse(defaultPublicKeyProvider);
    }

}
