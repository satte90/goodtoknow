package com.teliacompany.tiberius.base.server.auth.manager;

import com.teliacompany.tiberius.base.server.api.BaseErrors;
import com.teliacompany.tiberius.base.server.auth.key.PublicKeyResponse;
import com.teliacompany.webflux.error.exception.WebException;
import com.teliacompany.webflux.error.exception.client.ForbiddenException;
import com.teliacompany.webflux.error.exception.client.UnauthorizedException;
import com.teliacompany.webflux.error.exception.server.InternalServerErrorException;
import com.teliacompany.tiberius.base.server.auth.TiberiusJwtAuthenticationToken;
import com.teliacompany.tiberius.base.server.auth.key.PublicKeyProvider;
import com.teliacompany.tiberius.base.server.condition.TiberiusUserAuthEnabledCondition;
import com.teliacompany.tiberius.base.server.config.security.TiberiusSecurityConfigurer;
import com.teliacompany.tiberius.base.server.integration.slack.SlackPanicClient;
import com.teliacompany.tiberius.user.api.v1.Role;
import com.teliacompany.tiberius.user.auth.TiberiusAuthJwtsUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.JwtException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Authentication manager, authenticates using the jwt sent in the Authentication object. If JWT is valid returns a verified TiberiusJwtAuthenticationToken.
 */
@Primary
@Conditional(TiberiusUserAuthEnabledCondition.class)
@Component
public class TiberiusJwtAuthenticationManager implements TiberiusAuthenticationManager {
    private static final Logger LOG = LoggerFactory.getLogger(TiberiusJwtAuthenticationManager.class);
    private static final String AUTHENTICATION_SYSTEM = "Authentication";

    private final ConfigurableEnvironment env;
    private final String applicationName;
    private final List<String> endpointsWithNoRoleRequirement = new ArrayList<>();
    private final PublicKeyProvider publicKeyProvider;
    private final AntPathMatcher pathMatcher;

    public TiberiusJwtAuthenticationManager(ConfigurableEnvironment configurableEnvironment,
                                            List<TiberiusSecurityConfigurer> tiberiusSecurityConfigurers,
                                            List<PublicKeyProvider> publicKeyProviders,
                                            @Value("${spring.application.name:n/a}") String applicationName,
                                            @Value("${tiberius.path.prefix}") String basePath) {

        LOG.info("Tiberius authentication enabled");

        this.env = configurableEnvironment;
        this.applicationName = applicationName;
        this.pathMatcher = new AntPathMatcher();

        tiberiusSecurityConfigurers.forEach(c -> {
            List<String> prefixedEndpoints = c.noRolesRequiredEndpoints().stream()
                    .map(openEndpoint -> StringUtils.removeEnd(basePath, "/") + StringUtils.prependIfMissing(openEndpoint, "/"))
                    .map(endpoint -> StringUtils.prependIfMissing(endpoint, "/"))
                    .map(endpoint -> StringUtils.removeEnd(endpoint, "/"))
                    .collect(Collectors.toList());
            this.endpointsWithNoRoleRequirement.addAll(prefixedEndpoints);
        });
        LOG.info("Endpoints with no role requirement: {}", this.endpointsWithNoRoleRequirement);

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
    public Mono<Authentication> authenticate(Authentication authentication) {
        //Authentication should be of type TiberiusJwtAuthenticationToken
        final String jwt = ((TiberiusJwtAuthenticationToken) authentication).getJwt();
        final String requestPath = ((TiberiusJwtAuthenticationToken) authentication).getRequestPath();
        return validateJwtAndGetClaims(jwt)
                .map(claims -> {
                    final String tcad = claims.get(TiberiusAuthJwtsUtils.CLAIM_TCAD, String.class);
                    final String role = claims.get(TiberiusAuthJwtsUtils.CLAIM_ROLE, String.class);
                    final boolean isSuperuser = Boolean.TRUE.equals(claims.get(TiberiusAuthJwtsUtils.CLAIM_SUPERUSER, Boolean.class));

                    if(role != null) {
                        //Add superuser role if role is not already superuser but isSuperuser is true
                        if(!Role.SUPERUSER.name().equals(role) && isSuperuser) {
                            return TiberiusJwtAuthenticationToken.verified(requestPath, jwt, tcad, role, Role.SUPERUSER.name());
                        }
                        return TiberiusJwtAuthenticationToken.verified(requestPath, jwt, tcad, role);
                    } else if(endpointsWithNoRoleRequirement.stream().anyMatch(noRolePathPattern -> pathMatcher.match(noRolePathPattern, StringUtils.prependIfMissing(requestPath, "/")))) {
                        return TiberiusJwtAuthenticationToken.verified(requestPath, jwt, tcad);
                    }
                    throw new ForbiddenException(BaseErrors.AUTHENTICATION_FAILED, "User has no role selected, endpoint requires a role to be set. Endpoint requested: " + requestPath);
                });
    }

    @Override
    public Authentication authenticateTcad(Authentication authentication, String tcad) {
        final boolean hasAdminAuthority = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("SUPERUSER"));
        if(!hasAdminAuthority && Objects.equals(tcad, authentication.getPrincipal())) {
            throw new ForbiddenException(BaseErrors.AUTHENTICATION_FAILED, "Tcad does not match tcad in JWT");
        }
        return authentication;
    }

    private Mono<Claims> validateJwtAndGetClaims(String jwt) {
        return parseKeyId(jwt)
                .flatMap(publicKeyProvider::getPublicKey)
                .map(publicKey -> TiberiusAuthJwtsUtils.validateSignedJwt(publicKey, jwt))
                .onErrorMap(this::convertError)
                .map(Jwt::getBody);
    }

    private Mono<String> parseKeyId(String jwt) {
        try {
            return Mono.just(TiberiusAuthJwtsUtils.parseKeyId(jwt));
        } catch(NullPointerException | JwtException e) {
            throw convertError(e);
        }
    }

    private WebException convertError(Throwable throwable) {
        if(throwable instanceof JwtException) {
            return new UnauthorizedException("Invalid JWT", AUTHENTICATION_SYSTEM, throwable);
        }
        if(throwable instanceof NullPointerException) {
            return new ForbiddenException("Missing JWT", AUTHENTICATION_SYSTEM, throwable);
        }
        return new InternalServerErrorException("Could not parse JWT", AUTHENTICATION_SYSTEM, throwable);
    }
}
