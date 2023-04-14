package com.teliacompany.tiberius.base.server.auth.manager;

import com.teliacompany.tiberius.base.server.auth.key.PublicKeyResponse;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import reactor.core.publisher.Mono;

import java.security.PublicKey;

/**
 * TiberiusAuthenticationManager can either be TiberiusJwtAuthenticationManager which can authenticate Tiberius JWTs or DisabledAuthenticationManager
 */
@SuppressWarnings("unused")
public interface TiberiusAuthenticationManager extends ReactiveAuthenticationManager {
    Mono<PublicKeyResponse> getLatestPublicKey();

    /**
     * Use this to validate tcad equals jwt principal. Throws Forbidden exception if not equals. Otherwise returns the authentication object.
     */
    Authentication authenticateTcad(Authentication authentication, String tcad);
}
