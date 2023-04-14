package com.teliacompany.tiberius.base.server.auth.manager;

import com.teliacompany.tiberius.base.server.auth.TiberiusJwtAuthenticationToken;
import com.teliacompany.tiberius.base.server.auth.key.PublicKeyResponse;
import com.teliacompany.tiberius.base.server.condition.TiberiusUserAuthDisabledCondition;
import com.teliacompany.tiberius.user.api.v1.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Conditional;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Used when authentication has been disabled
 */
@Conditional(TiberiusUserAuthDisabledCondition.class)
@Component
public class TiberiusDisabledAuthenticationManager implements TiberiusAuthenticationManager {
    private static final Logger LOG = LoggerFactory.getLogger(TiberiusDisabledAuthenticationManager.class);

    @Override
    public Mono<PublicKeyResponse> getLatestPublicKey() {
        LOG.info("Not getting latest public key as tiberius user authentication is disabled");
        return Mono.just(PublicKeyResponse.disabled());
    }

    @Override
    public Authentication authenticateTcad(Authentication authentication, String tcad) {
        LOG.debug("Not validating tcad: {} as user authentication is disabled", tcad);
        return authentication;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        final TiberiusJwtAuthenticationToken tokenAuthentication = TiberiusJwtAuthenticationToken.verified(
                ((TiberiusJwtAuthenticationToken) authentication).getRequestPath(),
                "jwt",
                ((TiberiusJwtAuthenticationToken) authentication).getTcad(),
                Role.OURTELIA.name(),
                Role.TELEMARKETING.name(),
                Role.FIELDMARKETING.name(),
                Role.RETAILER.name(),
                Role.ORDERMANAGEMENT.name(),
                Role.SUPERUSER.name()
        );
        return Mono.just(tokenAuthentication);
    }
}
