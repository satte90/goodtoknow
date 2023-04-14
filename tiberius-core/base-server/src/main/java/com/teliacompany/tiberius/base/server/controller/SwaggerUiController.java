package com.teliacompany.tiberius.base.server.controller;

import com.teliacompany.tiberius.base.server.config.SwaggerConfig;
import io.swagger.v3.oas.annotations.Hidden;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

@Hidden
@RestController
public class SwaggerUiController {
    private static final Logger LOG = LoggerFactory.getLogger(SwaggerUiController.class);
    private String swaggerHtml;

    public SwaggerUiController(SwaggerConfig swaggerConfig) {
        String springDocBasePath = StringUtils.removeEnd(swaggerConfig.getSpringDocBasePath(), "/");
        String apiDocs = swaggerConfig.getApiDocs();
        if(springDocBasePath.length()==0) {
            apiDocs = StringUtils.removeStart(apiDocs, "/");
        }
        InputStream resourceStream = getClass().getClassLoader().getResourceAsStream("static/swagger/swagger.html");
        if(resourceStream != null) {
            StringBuilder textBuilder = new StringBuilder();
            try(Reader reader = new BufferedReader(new InputStreamReader(resourceStream, StandardCharsets.UTF_8))) {
                int c;
                while((c = reader.read()) != -1) {
                    textBuilder.append((char) c);
                }
                swaggerHtml = textBuilder.toString().replace("${swagger-base-path}", swaggerConfig.getSwaggerStaticPath())
                        .replace("${api-docs}", springDocBasePath + apiDocs);

            } catch(IOException e) {
                LOG.error("Could not read swagger html file", e);
                swaggerHtml = "Disabled";
            }
        } else {
            LOG.error("Swagger file not found");
            swaggerHtml = "disabled";
        }

    }

    /**
     * Redirects to swagger ui with configUrl set to /swagger/config.
     */
    @GetMapping(name = "static/swagger", path = "/swagger", produces = "text/html")
    public Mono<String> swagger() {
        return Mono.just(swaggerHtml);
    }
}
