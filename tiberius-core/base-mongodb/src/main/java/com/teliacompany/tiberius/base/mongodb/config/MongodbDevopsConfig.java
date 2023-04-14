package com.teliacompany.tiberius.base.mongodb.config;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "tiberius.mongodb.devops")
public class MongodbDevopsConfig extends HashMap<String, Map<String, String>> {
    public static final String MAX_LIMIT_KEY = "maxLimit";

    public Object get(String collectionName, String keyName) {
        return getOrDefault(collectionName, get("default")).get(keyName);
    }

    public int getMaxLimit(String collectionName) {
        return NumberUtils.toInt(getOrDefault(collectionName, get("default")).get(MAX_LIMIT_KEY));
    }
}
