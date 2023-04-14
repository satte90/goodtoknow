package com.teliacompany.tiberius.base.server.auth;

import com.teliacompany.tiberius.base.server.api.TiberiusHeaders;
import com.teliacompany.webflux.error.exception.client.UnauthorizedException;
import com.teliacompany.webflux.request.RequestProcessor;
import com.teliacompany.webflux.request.context.TransactionContext;
import com.teliacompany.webflux.request.processor.InternalRequestProcessor;
import com.teliacompany.webflux.request.utils.Constants;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

/**
 * Use contextRepository to load authentication from JWT. We don't store these, that would require a shared state, either distributed cache or database.
 * <p>
 * Loads the jwt from authentication header and strips Bearer prefix. Then sends the unvalidated jwt as an unvalidated TiberiusJwtAuthenticationToken to
 * tiberiusJwtAuthenticationManager which validates the token.
 */
@Component
public class TiberiusSecurityContextRepository implements ServerSecurityContextRepository {
    private final InternalRequestProcessor internalRequestProcessor;
    private final ReactiveAuthenticationManager tiberiusJwtAuthenticationManager;

    @Value("${management.server.port}")
    private int managementServerPort;

    public TiberiusSecurityContextRepository(InternalRequestProcessor internalRequestProcessor, ReactiveAuthenticationManager tiberiusJwtAuthenticationManager) {
        this.internalRequestProcessor = internalRequestProcessor;
        this.tiberiusJwtAuthenticationManager = tiberiusJwtAuthenticationManager;
    }

    @Override
    public Mono<Void> save(ServerWebExchange serverWebExchange, SecurityContext securityContext) {
        return Mono.empty();
    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange serverWebExchange) {
        final ServerHttpRequest request = serverWebExchange.getRequest();
        if(request.getURI().getPort() == managementServerPort) {
            // Allow requests to actuator. Seems impossible to permit request in Security config for a different port...
            return Mono.just(new SecurityContextImpl());
        }

        return internalRequestProcessor.processInternal(request)
                .withRequestObject(request)
                .withHandler(this::startAuthentication);
    }

    private Mono<SecurityContext> startAuthentication(ServerHttpRequest serverHttpRequest) {
        return createAuthenticationContext(serverHttpRequest)
                .map(this::getJwtFromRequest)
                .flatMap(this::authenticateJwt)
                .flatMap(this::getTransactionContext)
                .map(this::mutateHttpRequest)
                .map(this::createSecurityContext);
    }

    private Mono<AuthenticationContext> createAuthenticationContext(ServerHttpRequest httpRequest) {
        return Mono.just(new AuthenticationContext(httpRequest));
    }

    private AuthenticationContext getJwtFromRequest(AuthenticationContext authenticationContext) {
        final HttpHeaders headers = authenticationContext.httpRequest.getHeaders();
        final String jwt = Optional.ofNullable(headers.getFirst(AUTHORIZATION))
                .map(headerValue -> StringUtils.removeStart(headerValue, "Bearer "))
                .orElse(null);
        authenticationContext.setJwt(jwt);
        return authenticationContext;
    }

    private Mono<AuthenticationContext> authenticateJwt(AuthenticationContext authenticationContext) {
        final String requestPath = authenticationContext.httpRequest.getPath().value();
        final String tcad = authenticationContext.httpRequest.getHeaders().getFirst(TiberiusHeaders.X_TCAD);

        return tiberiusJwtAuthenticationManager.authenticate(TiberiusJwtAuthenticationToken.unverified(requestPath, authenticationContext.jwt, tcad))
                .switchIfEmpty(Mono.defer(() -> Mono.error(new UnauthorizedException("Invalid JWT"))))
                .map(authentication -> {
                    authenticationContext.setAuthentication((TiberiusJwtAuthenticationToken) authentication);
                    return authenticationContext;
                });
    }

    private Mono<AuthenticationContext> getTransactionContext(AuthenticationContext authenticationContext) {
        return RequestProcessor.getTransactionContext()
                .map(ctx -> {
                    authenticationContext.setTransactionContext(ctx);
                    return authenticationContext;
                });
    }

    private AuthenticationContext mutateHttpRequest(AuthenticationContext authenticationContext) {
        final String tcad = authenticationContext.authentication.getTcad();
        //Set tcad in MDC for logging
        MDC.put(Constants.MDC_TCAD_KEY, tcad);

        //Mutate the request so that when the REST controller gets the request it will contain tcad and the same TID
        authenticationContext.httpRequest.mutate()
                .header(Constants.HTTP_X_TCAD, tcad)
                .header(Constants.HTTP_TRANSACTION_ID_HEADER, authenticationContext.transactionContext.getTid())
                .build();
        return authenticationContext;
    }

    private SecurityContext createSecurityContext(AuthenticationContext authenticationContext) {
        return new SecurityContextImpl(authenticationContext.authentication);
    }

    private static class AuthenticationContext {
        private final ServerHttpRequest httpRequest;
        private String jwt;
        private TiberiusJwtAuthenticationToken authentication;
        private TransactionContext transactionContext;

        private AuthenticationContext(ServerHttpRequest httpRequest) {
            this.httpRequest = httpRequest;
        }

        public void setJwt(String jwt) {
            this.jwt = jwt;
        }

        private void setAuthentication(TiberiusJwtAuthenticationToken authentication) {
            this.authentication = authentication;
        }

        private void setTransactionContext(TransactionContext transactionContext) {
            this.transactionContext = transactionContext;
        }
    }

}
