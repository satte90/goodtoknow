package com.teliacompany.springfield.managetelephone.facade.config;

import com.teliacompany.springfield.managetelephone.facade.client.ManageTelephoneNumberFacadeClient;
import com.teliacompany.springfield.webflux.client.WebClient;
import com.teliacompany.springfield.webflux.client.WebClientBuilder;
import com.teliacompany.springfield.webflux.client.WebClientConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static com.teliacompany.springfield.webflux.utils.Constants.CONTENT_TYPE;


@Configuration
@ComponentScan(basePackages = {"com.teliacompany.springfield.managetelephone.facade.client"})
@Import(ManageTelephoneNumberFacadeClientConfiguration.class)
public class ManageTelephoneNumberFacadeAutoConfiguration {

    @Bean
    public ManageTelephoneNumberFacadeClient manageTelephoneNumberFacadeClient(ManageTelephoneNumberFacadeClientConfiguration config) {
        WebClientConfig wcconfig = WebClientConfig.builder()
                .withServiceName(ManageTelephoneNumberFacadeClientConfiguration.SERVICE_NAME)
                .withHost(config.getHost())
                .withBasePath(config.getEndpoint())
                .build();

        WebClient realClient = WebClientBuilder.withConfig(wcconfig)
                .defaultHeader(CONTENT_TYPE, "text/xml")
                .defaultHeader("Accept", "*/*")
                .defaultHeader("SOAPAction", "GetAvailableTelephoneNumbers")
                .build();
        return new ManageTelephoneNumberFacadeClient(realClient);
    }
}
