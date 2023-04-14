package com.teliacompany.tiberius.base.server.integration.slack;

import com.teliacompany.tiberius.base.server.config.ApplicationProperties;
import com.teliacompany.tiberius.base.server.config.SlackConfig;
import com.teliacompany.tiberius.base.server.config.VersionProperties;
import com.teliacompany.tiberius.base.server.service.CurrentTimeProvider;
import com.teliacompany.webflux.error.exception.server.InternalServerErrorException;
import com.teliacompany.webflux.request.client.WebClient;
import com.teliacompany.webflux.request.client.WebClientBuilder;
import com.teliacompany.webflux.request.client.WebClientConfig;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
public class DevOpsSlackClient {
    private static final Logger LOG = LoggerFactory.getLogger(DevOpsSlackClient.class);
    private final WebClient webClient;
    private final CurrentTimeProvider currentTimeProvider;

    private static final String SLACK_TEMPLATE = "{\"blocks\":[{\"type\":\"header\",\"text\":{\"type\":\"plain_text\",\"text\":\"%HEADER%\",\"emoji\":true}},{\"type\":\"divider\"},{\"type\":\"context\",\"elements\":[{\"type\":\"mrkdwn\",\"text\":\"` %STATUS% `\"},{\"type\":\"mrkdwn\",\"text\":\"` %ENV% `\"},{\"type\":\"mrkdwn\",\"text\":\"*Pod:* %POD%\"},{\"type\":\"mrkdwn\",\"text\":\"*Service:* `%SERVICE_VERSION%`\"},{\"type\":\"mrkdwn\",\"text\":\"*Core:* `%CORE_VERSION%`\"}]},{\"type\":\"context\",\"elements\":[{\"type\":\"mrkdwn\",\"text\":\"<%GRAFANA_LINK%|Grafana>\"},{\"type\":\"mrkdwn\",\"text\":\"<%SPLUNK_LINK%|Splunk>\"}]},{\"type\":\"divider\"}]}";
    private static final String GRAFANA_LINK_TEMPLATE = "https://grafana.dc.teliacompany.net/d/UjsGjzlGk/tiberius-services?orgId=1&from=%FROM%&to=%TO%&var-Datasource=%DATASOURCE%&var-Env=%ENV%&var-EnvExt=%ENV_EXT%&var-ServiceName=%SERVICE_NAME%&var-ServiceNameAlt=%SERVICE_NAME_ALT%&var-InstanceName=%INSTANCE_NAME%&var-Pods=All";
    private static final String SPLUNK_LINK_TEMPLATE = "https://tssplunkse.han.telia.se/en-GB/app/hid100005054/search?earliest=%FROM%&latest=%TO%&q=search%20appName=%22%SERVICE_NAME%%22";

    private final String applicationName;
    private final String environment;
    private final String tiberiusCoreVersion;
    private final String serviceVersion;

    private final String serviceStatusEndpoint;
    private final boolean isInstantMessagingDisabled;

    private String hostName;

    public DevOpsSlackClient(ApplicationProperties applicationProperties,
                             VersionProperties versionProperties,
                             SlackConfig slackConfig,
                             CurrentTimeProvider currentTimeProvider) {
        this.applicationName = StringUtils.replace(applicationProperties.getApplicationName(), "-", " ").toUpperCase();
        this.environment = applicationProperties.getActiveSpringProfiles().stream().findFirst().orElse("UNKNOWN");
        this.tiberiusCoreVersion = versionProperties.getTiberiusCoreVersion();
        this.serviceVersion = versionProperties.getAppVersion();
        this.currentTimeProvider = currentTimeProvider;
        this.serviceStatusEndpoint = slackConfig.getServiceStatusEndpoint();

        this.isInstantMessagingDisabled = !slackConfig.isEnabled() || applicationProperties.getActiveSpringProfiles()
                .stream()
                .anyMatch(p -> p.equalsIgnoreCase("local") || p.equalsIgnoreCase("componenttest"))
                || StringUtils.endsWith(serviceVersion, "-SNAPSHOT");

        WebClientConfig config = WebClientConfig.builder()
                .withServiceName("Slack")
                .withHost(slackConfig.getHost())
                .withProxyEnabled(slackConfig.isProxyEnabled(), slackConfig.getProxyHost(), slackConfig.getProxyPort())
                .withBasePath(slackConfig.getBasePath())
                .build();

        webClient = WebClientBuilder.withConfig(config).build();
    }

    @PostConstruct
    public void init() {
        hostName = Optional.ofNullable(System.getenv("HOSTNAME")).orElse("unknown-hostname-" + UUID.randomUUID().toString());
    }

    public Mono<String> postStartupMessage() {
        if(isInstantMessagingDisabled) {
            return Mono.just("Disabled");
        }

        String body = SLACK_TEMPLATE
                .replace("%HEADER%", String.format("%s %s", getEnvIcon(environment), applicationName))
                .replace("%STATUS%", "STARTED")
                .replace("%ENV%", environment)
                .replace("%POD%", hostName)
                .replace("%SERVICE_VERSION%", serviceVersion)
                .replace("%CORE_VERSION%", tiberiusCoreVersion)
                .replace("%GRAFANA_LINK%", getGrafanaLink())
                .replace("%SPLUNK_LINK%", getSplunkLink());

        return postSlackMessage(body);
    }

    public Mono<String> postShutdownMessage() {
        if(isInstantMessagingDisabled) {
            return Mono.just("Disabled");
        }

        String body = SLACK_TEMPLATE
                .replace("%HEADER%", String.format(":red_circle: %s", applicationName))
                .replace("%STATUS%", "STOPPED")
                .replace("%ENV%", environment)
                .replace("%POD%", hostName)
                .replace("%SERVICE_VERSION%", serviceVersion)
                .replace("%CORE_VERSION%", tiberiusCoreVersion)
                .replace("%GRAFANA_LINK%", getGrafanaLink())
                .replace("%SPLUNK_LINK%", getSplunkLink());

        return postSlackMessage(body);
    }

    public Mono<String> postSlackMessage(String body) {
        return webClient.post(serviceStatusEndpoint)
                .body(body)
                .retrieve(String.class)
                .timeout(Duration.ofSeconds(10))
                .map(response -> {
                    LOG.debug("Got answer \"{}\" from {}", response.getBody().orElse("N/A"), webClient.getServiceName());
                    return response.getBody().orElseThrow(() -> new InternalServerErrorException("Invalid response from slack"));
                });
    }

    private static String getEnvIcon(String environment) {
        if("dev".equalsIgnoreCase(environment)) {
            return ":large_yellow_circle:";
        }
        if("sit".equalsIgnoreCase(environment)) {
            return ":large_orange_circle:";
        }
        if("at".equalsIgnoreCase(environment)) {
            return ":large_blue_circle:";
        }
        if("beta".equalsIgnoreCase(environment)) {
            return ":large_purple_circle:";
        }
        if("prod".equalsIgnoreCase(environment)) {
            return ":large_green_circle:";
        }
        return ":white_circle:";
    }

    private String getSplunkLink() {
        return SPLUNK_LINK_TEMPLATE
                .replace("%FROM%", String.valueOf(currentTimeProvider.getInstantNow().getEpochSecond() - 900)) // Now - 15 min
                .replace("%TO%", String.valueOf(currentTimeProvider.getInstantNow().getEpochSecond() + 900)) // Now + 15 min
                .replace("%SERVICE_NAME%", applicationName.toLowerCase().replace(" ", "-"));
    }

    private String getGrafanaLink() {
        return GRAFANA_LINK_TEMPLATE
                .replace("%FROM%", String.valueOf(currentTimeProvider.getEpochMillisNow() - 1_800_000)) // Now - 30 min
                .replace("%TO%", String.valueOf(currentTimeProvider.getEpochMillisNow() + 1_800_000)) // Now + 30 min
                .replace("%ENV%", environment)
                .replace("%ENV_EXT%", getEnvExt())
                .replace("%DATASOURCE%", getDatasource())
                .replace("%SERVICE_NAME%", applicationName.toLowerCase().replace(" ", "-"))
                .replace("%SERVICE_NAME_ALT%", applicationName.toLowerCase().replace(" ", "_"))
                .replace("%INSTANCE_NAME%", applicationName.toLowerCase().replace(" ", "-") + getEnvExt());
    }

    private String getEnvExt() {
        return environment.equalsIgnoreCase("prod") ? "" : "-" + environment;
    }

    private String getDatasource() {
        if(environment.equalsIgnoreCase("prod") || environment.equalsIgnoreCase("beta")) {
            return "Prometheus";
        }
        return "Prometheus-TEST";
    }
}
