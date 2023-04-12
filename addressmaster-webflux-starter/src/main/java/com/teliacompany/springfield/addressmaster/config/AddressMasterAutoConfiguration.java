package com.teliacompany.springfield.addressmaster.config;

import com.teliacompany.springfield.addressmaster.client.AddressMasterWebClient;
import com.teliacompany.springfield.webflux.client.WebClient;
import com.teliacompany.springfield.webflux.client.WebClientBuilder;
import com.teliacompany.springfield.webflux.client.WebClientConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;


@Configuration
@ComponentScan(basePackages = {"com.teliacompany.springfield.addressmaster.client"})
@Import(AddressMasterClientConfiguration.class)
public class AddressMasterAutoConfiguration {

    @Bean
    public AddressMasterWebClient addressMasterWebClient(AddressMasterClientConfiguration config) {
        WebClientConfig wcConfig = WebClientConfig.builder()
                .withServiceName(AddressMasterClientConfiguration.SERVICE_NAME)
                .withHost(config.getHost())
                .withBasePath(config.getEndpoint())
                .build();

        String basicAuth = Base64.getEncoder().encodeToString((config.getUsername() + ":" + config.getPassword()).getBytes(StandardCharsets.UTF_8));
        WebClient realClient = WebClientBuilder.withConfig(wcConfig)
                .defaultHeader(CONTENT_TYPE, "text/xml;charset=UTF-8")
                .defaultHeader("Authorization", "Basic " + basicAuth)
                .defaultHeader("SOAPAction", "")
                .build();
        return new AddressMasterWebClient(realClient, config);
    }
}
