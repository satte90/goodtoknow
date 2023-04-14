package com.teliacompany.tiberius.base.server.auth.manager;

import com.teliacompany.tiberius.base.server.auth.TiberiusJwtAuthenticationToken;
import com.teliacompany.tiberius.base.server.auth.key.PublicKeyProvider;
import com.teliacompany.tiberius.base.server.auth.key.PublicKeyResponse;
import com.teliacompany.tiberius.base.server.condition.TiberiusUserAuthVaultOnlyCondition;
import com.teliacompany.tiberius.base.server.integration.slack.SlackPanicClient;
import com.teliacompany.tiberius.user.api.v1.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Used when authentication has been disabled but tiberius vault enabled
 */
@Conditional(TiberiusUserAuthVaultOnlyCondition.class)
@Component
public class TiberiusVaultOnlyAuthenticationManager extends TiberiusDisabledAuthenticationManager implements TiberiusAuthenticationManager {
    private static final Logger LOG = LoggerFactory.getLogger(TiberiusVaultOnlyAuthenticationManager.class);
    private final PublicKeyProvider publicKeyProvider;
    private final ConfigurableEnvironment env;
    private final String applicationName;

    public TiberiusVaultOnlyAuthenticationManager(ConfigurableEnvironment configurableEnvironment,
                                                  List<PublicKeyProvider> publicKeyProviders,
                                                  @Value("${spring.application.name:n/a}") String applicationName) {
        this.env = configurableEnvironment;
        this.applicationName = applicationName;
        this.publicKeyProvider = PublicKeyProvider.getPrioritizedKeyProvider(publicKeyProviders);
        LOG.info("PublicKeyProvider = {}", publicKeyProvider.getClass().getName());
    }

    @Override
    public Mono<PublicKeyResponse> getLatestPublicKey() {
        return publicKeyProvider.getLatestPublicKey()
                .map(PublicKeyResponse::success)
                .onErrorResume(e -> {
                    final String message = "Could not fetch latest key from Tiberius User Auth!";
                    SlackPanicClient.postSlackPanicMessage(env, applicationName, message);
                    LOG.error(message, e);
                    return Mono.just(PublicKeyResponse.failure());
                });
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
