package com.teliacompany.tiberius.base.hazelcast.config;

import com.teliacompany.tiberius.base.hazelcast.exception.HazelcastMissingChartPropertiesException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.List;

@Import(HazelcastVersionProperties.class)
@Configuration
public class TiberiusHazelcastProperties {
    private static final Logger LOG = LoggerFactory.getLogger(TiberiusHazelcastProperties.class);

    private static final String AUTOMATIC = "automatic";

    private final String tokenFilePath;
    private final boolean discoveryEnabled;
    private final String instanceName; //Should be the same as releaseName defined in Jenkinsfile
    private final String clusterName; //Changed when/if hazelcast version changes to a non backwards compatible version
    private final boolean localMode;
    private final boolean testMode;

    private final HazelcastVersionProperties versions;

    public TiberiusHazelcastProperties(
            HazelcastVersionProperties hazelcastVersionProperties,
            @Value("${hazelcast.shutdownhook.policy:NOT_SET}") String shutDownPolicy,
            @Value("${hazelcast.graceful.shutdown.max.wait:NOT_SET}") String shutDownMaxWait,
            @Value("${hazelcast.token.filepath:/var/run/secrets/kubernetes.io/serviceaccount/token}") String tokenFilePath,
            @Value("${hazelcast.discovery.enabled:true}") boolean discoveryEnabled,
            @Value("${hazelcast.instance.name:automatic}") String instanceName,
            @Value("${hazelcast.cluster.name:automatic}") String clusterName,
            @Value("${hazelcast.cluster.name.appVersionSuffix:false}") boolean useApplicationVersionInClusterName,
            @Value("${spring.application.name}") String appName,
            @Value("${build.version}") String appVersion,
            @Value("#{T(java.util.Arrays).asList('${spring.profiles.active}')}") List<String> activeSpringProfiles) {

        this.versions = hazelcastVersionProperties;
        this.tokenFilePath = tokenFilePath;
        this.discoveryEnabled = discoveryEnabled;

        this.localMode = activeSpringProfiles.contains("local");
        this.testMode = activeSpringProfiles.contains("componenttest");

        if(!localMode && !testMode && (shutDownPolicy.equals("NOT_SET") || shutDownMaxWait.equals("NOT_SET"))) {
            throw new HazelcastMissingChartPropertiesException();
        }

        if(AUTOMATIC.equalsIgnoreCase(instanceName)) {
            //Filter out local profile as it should never be the main profile, it should always be used together with another profile
            //Filter out prod profile as instance name usually (never?) have the -prod suffix
            String mainSpringProfile = activeSpringProfiles.stream()
                    .filter(profile -> !"local".equalsIgnoreCase(profile))
                    .filter(profile -> !"prod".equalsIgnoreCase(profile))
                    .findFirst()
                    .map(p -> "-" + p)
                    .orElse("");

            String appVersionSuffix = "";
            if(useApplicationVersionInClusterName && appVersion != null) {
                appVersionSuffix = "-" + appVersion.replace(".", "_");
            }
            this.instanceName = appName + appVersionSuffix + mainSpringProfile;
        } else {
            this.instanceName = instanceName;
        }

        String versionSuffix = "";
        if(versions.getHazelcastVersion() != null) {
            String[] hazelcastVersions = versions.getHazelcastVersion().split("\\.");
            String major = hazelcastVersions[0];
            String minor = hazelcastVersions[1];
            versionSuffix = "_v" + major + "_" + minor;
        }

        if(AUTOMATIC.equalsIgnoreCase(clusterName)) {
            String appVersionSuffix = "";
            if(useApplicationVersionInClusterName && appVersion != null) {
                appVersionSuffix = "-" + appVersion.replace(".", "_");
            }
            this.clusterName = this.instanceName + appVersionSuffix + versionSuffix;
        } else {
            this.clusterName = clusterName;
        }
    }

    public String getInstanceName() {
        return instanceName;
    }

    public boolean isLocalMode() {
        return localMode;
    }

    public boolean isTestMode() {
        return testMode;
    }

    public String getTokenFilePath() {
        return tokenFilePath;
    }

    public boolean isDiscoveryEnabled() {
        return discoveryEnabled;
    }

    public String getClusterName() {
        return clusterName;
    }

    public HazelcastVersionProperties getVersions() {
        return versions;
    }
}
