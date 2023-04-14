package com.teliacompany.tiberius.base.server.config.security;

import com.teliacompany.tiberius.base.server.auth.TiberiusSecurityContextRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.savedrequest.NoOpServerRequestCache;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher.MatchResult;
import org.springframework.web.server.ServerWebExchange;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableReactiveMethodSecurity
@EnableWebFluxSecurity
@ComponentScan
public class TiberiusSecurityConfig {
    private static final Logger LOG = LoggerFactory.getLogger(TiberiusSecurityConfig.class);

    @Value("${management.server.port}")
    private int managementServerPort;

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http,
                                                            ReactiveAuthenticationManager reactiveAuthenticationManager,
                                                            TiberiusSecurityContextRepository securityContextRepository,
                                                            List<TiberiusSecurityConfigurer> tiberiusSecurityConfigurers,
                                                            @Value("${tiberius.path.prefix}") String basePath) {

        final List<String> allOpenEndpoints = new ArrayList<>();
        tiberiusSecurityConfigurers.forEach(configurer -> allOpenEndpoints.addAll(configurer.additionalUnsecuredEndpoints()));

        final String[] prefixedOpenEndpoints = allOpenEndpoints.stream()
                .map(openEndpoint -> StringUtils.removeEnd(basePath, "/") + StringUtils.prependIfMissing(openEndpoint, "/"))
                .map(endpoint -> StringUtils.prependIfMissing(endpoint, "/"))
                .toArray(String[]::new);

        LOG.info("Endpoints requiring no authentication: {}", Arrays.asList(prefixedOpenEndpoints));

        // see https://github.com/spring-projects/spring-security/issues/6552
        // """ The reason is that enabling Spring Security causes the WebSession to be read.
        // """ When Spring WebFlux tries to resolve the WebSession it looks in the SESSION
        // """ cookie for the id to resolve and finds that the session id is invalid.
        // """ Since the session id is invalid, Spring WebFlux invalidates the SESSION cookie.
        // https://github.com/spring-projects/spring-security/issues/7157
        // Workaround. Disable request cache
        http.requestCache().requestCache(NoOpServerRequestCache.getInstance());

        // @formatter:off
        return http
                .cors()
                    .disable()
                .csrf()
                    .disable()
                .authenticationManager(reactiveAuthenticationManager)
                .securityContextRepository(securityContextRepository)
                .authorizeExchange()
                    .pathMatchers(prefixedOpenEndpoints)
                        .permitAll()
                    .pathMatchers("/webjars/swagger-ui/**")
                        .permitAll()
                    .pathMatchers(basePath + "/webjars/swagger-ui/**")
                        .permitAll()
                    .matchers(forActuatorPort())
                        .permitAll()
                    .anyExchange()
                        .authenticated()
                .and()
                .build();
        // @formatter:on
    }


    private ServerWebExchangeMatcher forActuatorPort() {
        return (ServerWebExchange webExchange) -> managementServerPort == webExchange.getRequest().getURI().getPort() ? MatchResult.match() : MatchResult.notMatch();
    }
}
