package com.teliacompany.tiberius.base.server.service;

import com.teliacompany.webflux.request.context.ContextWrapper;
import com.teliacompany.webflux.request.processor.error.ErrorAttributesProvider;
import com.teliacompany.webflux.request.processor.error.ReadOnlyWebException;
import com.teliacompany.tiberius.base.server.config.ApplicationProperties;
import com.teliacompany.tiberius.base.server.config.VersionProperties;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Error attributes provider
 * This is picked up by Request Webflux Starter and will be used to automatically add error attributes to thrown WebExceptions
 */
@SuppressWarnings("unused")
@Component
public class TiberiusErrorAttributesProvider implements ErrorAttributesProvider {
    private static final String HID = "hid100003852";

    private final ApplicationProperties appProps;
    private final VersionProperties versionProperties;

    public TiberiusErrorAttributesProvider(ApplicationProperties applicationProperties, VersionProperties versionProperties) {
        this.appProps = applicationProperties;
        this.versionProperties = versionProperties;
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    @Override
    public Map<String, Object> getErrorAttributes(ReadOnlyWebException webException, ContextWrapper contextData) {
        Map<String, Object> map = new HashMap<>();
        map.put("applicationName", appProps.getApplicationName());
        map.put("applicationVersion", versionProperties.getAppVersion());
        map.put("coreVersion", versionProperties.getTiberiusCoreVersion());
        map.put("mainSpringProfile", appProps.getMainSpringProfile());

        // https://tssplunkse.han.telia.se/en-GB/app/hid100003852/search?q=search%20source%3D%22%2Fopt%2Fapps%2Flogs%2Fkubernetes%2Ftse%2Fdeployments%2Ftiberius-products-at%2F*%22%7C%20spath%20transactionId%20%7C%20search%20transactionId%3D%2216de68a84acecd97152bfa670fd99a99%22&display.page.search.mode=smart&dispatch.sample_ratio=1&earliest=1614151800&latest=1614164400
        String tid = contextData.getTransactionContext().getTid();
        long startTimestamp = Instant.now().getEpochSecond() - 480; //-8 minutes
        long endTimestamp = Instant.now().getEpochSecond() + 60; //+2 minutes
        String profileSuffix = "prod".equalsIgnoreCase(appProps.getMainSpringProfile()) ? "" : "-" + appProps.getMainSpringProfile();
        String errorLink = new StringBuilder()
                .append("https://tssplunkse.han.telia.se/en-GB/app/")
                .append(HID)
                .append("/search?q=search%20")
                .append(urlEncode("source=\"/opt/apps/logs/kubernetes/tse/deployments/%s%s/*\"", appProps.getApplicationName(), profileSuffix))
                .append(urlEncode("| spath transactionId | search transactionId=\"%s\"", tid))
                .append("&display.page.search.mode=smart&dispatch.sample_ratio=1")
                .append("&earliest=").append(startTimestamp)
                .append("&latest=").append(endTimestamp)
                .toString();

        map.put("errorLink", errorLink);
        return map;
    }

    private static String urlEncode(String s, Object... replacements) {
        String formatted = String.format(s, replacements);
        return URLEncoder.encode(formatted, StandardCharsets.UTF_8);
    }
}
