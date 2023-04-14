package com.teliacompany.tiberius.base.server.config;

import com.teliacompany.tiberius.base.server.api.TiberiusHeaders;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;
import io.swagger.v3.oas.models.servers.Server;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Configuration
public class SpringDocConfig {
    private static final Logger LOG = LoggerFactory.getLogger(SpringDocConfig.class);

    @Bean
    public OpenAPI customOpenAPI(ApplicationProperties applicationProperties, SwaggerConfig swaggerConfig, VersionProperties versionProperties) {
        // This configures swagger.
        // The components part defines components that can be used, defining a component without using them has no effect.
        // We define a SecuritySchemes component for the x-tcad header (normally this would be an API key), which it basically is but we call it x-tcad
        //   This will create the "Authorize" button in swagger UI where you can enter your API key, which in our case is a tcad id.
        // Then we define a security object where we refer to the tcadHeader api key, and it will be used for all endpoints
        // In the Info block we define information about the API.
        String apiName = WordUtils.capitalize(StringUtils.replace(applicationProperties.getApplicationName(), "-", " ")) + " API";
        String apiVersion = StringUtils.removeEndIgnoreCase(versionProperties.getAppVersion(), "-SNAPSHOT");
        apiVersion = apiVersion.substring(0, apiVersion.lastIndexOf('.'));

        LOG.info("Creating customOpenAPI bean:");
        LOG.info("* api name='{}'", apiName);
        LOG.info("* api version='{}'", apiVersion);
        LOG.info("* springDocBasePath='{}'", swaggerConfig.getSpringDocBasePath());

        final String clientArtifactId = StringUtils.replace(applicationProperties.getArtifactId(), "-server", "-client");
        final String description = String.format("<p>%s</p>" +
                        "<h3>Maven Dependency:</h3>" +
                        "<pre>" +
                        "&lt;dependency&gt;<br/>" +
                        "&nbsp;&nbsp;&nbsp;&nbsp;&lt;groupId&gt;%s&lt;/groupId&gt;<br/>" +
                        "&nbsp;&nbsp;&nbsp;&nbsp;&lt;artifactId&gt;%s&lt;/artifactId&gt;<br/>" +
                        "&nbsp;&nbsp;&nbsp;&nbsp;&lt;version&gt;%s&lt;/version&gt;<br/>" +
                        "&lt;/dependency&gt;" +
                        "</pre>" +
                        "<h3>Contact</h3>" +
                        "<a style=\"text-decoration: none; position:relative; top:5px;\" href=\"https://telianorge.slack.com/archives/C02355VHVD2\">Chat with Our Telia</a>",
                applicationProperties.getApplicationDescription(), applicationProperties.getGroup(), clientArtifactId, versionProperties.getAppVersion());

        final String env = applicationProperties.getMainSpringProfile();
        String testHttpPart = "test.";
        String localExtraDescription = "";

        if(env.equalsIgnoreCase("prod") || env.equalsIgnoreCase("beta")) {
            testHttpPart = "";
        }

        if(applicationProperties.getActiveSpringProfiles().contains("local")) {
            localExtraDescription = "<br/><h4>Running locally</h4>" +
                    "When running locally authentication is generally disabled, enable it by setting:" +
                    "<h5>tiberius.user.auth.enabled=true</h5>" +
                    "Then to acquire a JWT, start Tiberius User Auth (TUA) on port 8082 and configure " +
                    applicationProperties.getApplicationName() + " to call TUA on this URI like so:" +
                    "<h5>tiberius.user.auth.host=http://localhost:8082</h5>" +
                    "Then call the local login endpoint:<br/>localhost:8082/tiberius/user/auth/local/login/yourTcad/SUPERUSER<br/>" +
                    "change yourTcad to the tcad you want to use/test and change admin to the role you want to have. " +
                    "SUPERUSER has all authorities<br/><br/>";
        }

        //For details and documentation of OpenAPI see: https://github.com/OAI/OpenAPI-Specification/blob/3.0.1/versions/3.0.1.md#securitySchemeObject
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("TiberiusJWT", new SecurityScheme()
                                .type(Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .name("Authorization")
                                .description("Enter Tiberius JWT. The JWT can be acquired by logging in to Tiberius, " +
                                        "i.e. call login endpoint on tiberius-user-auth service. This endpoint requires a valid EAM token" +
                                        "to be provided in the " + TiberiusHeaders.X_AUGUSTUS_TOKEN + " header. You can get this by logging" +
                                        "in to EAM, then request https://" + env + "-augustus." + testHttpPart + "telia.se/api/user/token" +
                                        localExtraDescription)
                        )
                )
                .servers(Collections.singletonList(new Server()
                        .url(swaggerConfig.getServerPath())
                ))
                .security(Collections.singletonList(new SecurityRequirement().addList("TiberiusJWT")))
                .info(new Info()
                        .title(apiName)
                        .version(apiVersion)
                        .description(description)
                        .contact(new Contact()
                                .name("Our Telia")
                                .email("DL-Brutus@tms.telia.se")
                                .url("https://diva.teliacompany.net/confluence/display/DCVT/DigitalChannels+Our+Telia")
                        )
                );
    }
}
