package com.teliacompany.tiberius.base.server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class IntegrationUsageStatuses {
    private final boolean hazelcastEnabled;
    private final boolean mongodbEnabled;
    private final boolean kafkaEnabled;
    private final boolean ldapEnabled;
    private final String nonMongoDatabaseUsed;

    public IntegrationUsageStatuses(ApplicationContext applicationContext,
                                    @Value("${spring.data.mongodb.uri:#{null}}") String mongodbUri,
                                    @Value("${spring.datasource.url:#{null}}") String databaseUrl,
                                    @Value("${kafka.enabled:false}") boolean kafkaEnabled,
                                    @Value("${ldap.context.source.url:#{null}}") String ldapContextSourceUrl) {

        this.hazelcastEnabled = applicationContext.containsBean("TiberiusHazelcastCacheRegistry");
        this.mongodbEnabled = mongodbUri != null; // Simple check if the property exist, assume it is used...
        this.kafkaEnabled = kafkaEnabled;
        this.ldapEnabled = ldapContextSourceUrl != null; // Simple check if the property exist, assume it is used...

        if(databaseUrl != null) {
            this.nonMongoDatabaseUsed = Arrays.stream(databaseUrl.split(":"))
                    .filter(part -> part.startsWith("@") && part.length() > 1)
                    .findFirst()
                    .map(part -> part.substring(1))
                    .orElse(null);
        } else {
            this.nonMongoDatabaseUsed = null;
        }
    }

    public boolean isHazelcastEnabled() {
        return hazelcastEnabled;
    }

    public boolean isMongodbEnabled() {
        return mongodbEnabled;
    }

    public boolean isLdapEnabled() {
        return ldapEnabled;
    }

    public boolean isKafkaEnabled() {
        return kafkaEnabled;
    }

    public boolean isNonMongoDatabaseEnabled() {
        return nonMongoDatabaseUsed != null;
    }

    public String getNonMongoDatabaseUsed() {
        return nonMongoDatabaseUsed;
    }
}
