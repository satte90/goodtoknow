package com.teliacompany.tiberius.base.toca.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class TocaCorsConfig {
    private static final Logger LOG = LoggerFactory.getLogger(TocaCorsConfig.class);
    private final List<String> allowedOrigins;

    public TocaCorsConfig(@Value("${tiberius.toca.cors.allowedOrigins}") String allowedOrigins) {
        this.allowedOrigins = Arrays.asList(allowedOrigins.split(","));
    }

    @Bean
    CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setMaxAge(8000L);
        corsConfig.setAllowedMethods(List.of("PUT", "POST", "GET", "DELETE", "PATCH"));
        corsConfig.addAllowedHeader("*");
        corsConfig.setAllowedOriginPatterns(allowedOrigins);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        LOG.info("Toca CORS configured: {}", corsConfig.getAllowedOriginPatterns());
        return new CorsWebFilter(source);
    }

}
