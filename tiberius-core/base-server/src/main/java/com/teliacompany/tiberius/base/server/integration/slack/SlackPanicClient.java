package com.teliacompany.tiberius.base.server.integration.slack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.lang.Nullable;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static com.teliacompany.tiberius.base.server.integration.slack.DefaultSlackPanicProperties.BASE_ENDPOINT;
import static com.teliacompany.tiberius.base.server.integration.slack.DefaultSlackPanicProperties.DEFAULT_ENDPOINT;
import static com.teliacompany.tiberius.base.server.integration.slack.DefaultSlackPanicProperties.DEFAULT_PROXY_ENABLED;
import static com.teliacompany.tiberius.base.server.integration.slack.DefaultSlackPanicProperties.DEFAULT_PROXY_HOST;
import static com.teliacompany.tiberius.base.server.integration.slack.DefaultSlackPanicProperties.DEFAULT_PROXY_PORT;
import static com.teliacompany.tiberius.base.server.integration.slack.DefaultSlackPanicProperties.HOST;
import static com.teliacompany.tiberius.base.server.integration.slack.DefaultSlackPanicProperties.VERSION;

/**
 * Generally do not use this client, it uses old school, non reactive ways to post http messages
 */
@SuppressWarnings("DuplicatedCode")
public final class SlackPanicClient {
    private static final Logger LOG = LoggerFactory.getLogger(SlackPanicClient.class);
    private static final String SLACK_TEMPLATE = "{\"text\": \":rotating_light:*%HEADER%*\\n%PANIC_MESSAGE%\"}";

    private SlackPanicClient() {
        //Not to be instantiated
    }

    /**
     * Post a message with the startup error using old-school HttpURLConnection.
     *
     * @param env             - ConfigurableEnvironment
     * @param applicationName - name of application (poster)
     * @param message         - message to be posted
     */
    public static void postSlackPanicMessage(@Nullable ConfigurableEnvironment env, String applicationName, String message) {
        final boolean isSlackEnabled = "true".equalsIgnoreCase(getFromEnvOrDefault(env, "tiberius.slack.devops.enabled", "false"));
        if(isSlackEnabled) {
            final String host = getFromEnvOrDefault(env, "tiberius.slack.devops.host", HOST);
            final String baseEndpoint = getFromEnvOrDefault(env, "tiberius.slack.devops.base.endpoint", BASE_ENDPOINT);
            final String defaultDevopsChannel = getFromEnvOrDefault(env, "tiberius.slack.devops.default.endpoint", DEFAULT_ENDPOINT);

            String request = SLACK_TEMPLATE
                    .replace("%HEADER%", applicationName)
                    .replace("%PANIC_MESSAGE%", message);

            String url = host + "/" + baseEndpoint + defaultDevopsChannel;

            final boolean proxyEnabled = getFromEnvOrDefault(env, "tiberius.slack.devops.proxy.enabled", DEFAULT_PROXY_ENABLED);
            final String proxyHost = getFromEnvOrDefault(env, "tiberius.slack.devops.proxy.host", DEFAULT_PROXY_HOST);
            final int proxyPort = Integer.parseInt(getFromEnvOrDefault(env, "tiberius.slack.devops.proxy.port", String.valueOf(DEFAULT_PROXY_PORT)));

            postSlackPanicMessage(url, request, proxyEnabled, proxyHost, proxyPort);
        }
    }

    private static String getFromEnvOrDefault(@Nullable ConfigurableEnvironment env, String key, String defaultValue) {
        return env != null ? env.getProperty(key, defaultValue) : defaultValue;
    }

    private static boolean getFromEnvOrDefault(@Nullable ConfigurableEnvironment env, String key, boolean defaultValue) {
        return env != null ? env.getProperty(key, Boolean.class, defaultValue) : defaultValue;
    }

    /**
     * Post a message with the startup error using old-school HttpURLConnection.
     *
     * @param url     - mattermost url
     * @param request - request object
     */
    private static void postSlackPanicMessage(String url, String request, boolean proxyEnabled, String proxyHost, int proxyPort) {
        HttpURLConnection con = null;
        try {
            byte[] postData = request.getBytes(StandardCharsets.UTF_8);

            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));

            URL myurl = new URL(url);
            con = (HttpURLConnection) (proxyEnabled ? myurl.openConnection(proxy) : myurl.openConnection());
            con.setDoOutput(true);
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", "Java client");
            con.setRequestProperty("Content-Type", "application/json; utf-8");

            try(DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
                wr.write(postData, 0, postData.length);
            }

            StringBuilder content;

            try(BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), Charset.defaultCharset()))) {
                String line;
                content = new StringBuilder();
                while((line = br.readLine()) != null) {
                    content.append(line);
                    content.append(System.lineSeparator());
                }
            }
        } catch(IOException e) {
            LOG.error("Could not post slack panic message", e);
        } finally {
            if(con != null) {
                con.disconnect();
            }
        }
    }
}
