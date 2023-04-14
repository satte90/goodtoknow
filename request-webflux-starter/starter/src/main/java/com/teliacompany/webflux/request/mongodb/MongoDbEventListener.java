package com.teliacompany.webflux.request.mongodb;

import com.teliacompany.webflux.request.log.RequestLogger;
import com.teliacompany.webflux.request.log.RequestLoggingOptions;
import com.teliacompany.webflux.request.utils.Constants;
import org.apache.logging.log4j.Level;
import org.bson.Document;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterDeleteEvent;
import org.springframework.data.mongodb.core.mapping.event.AfterLoadEvent;
import org.springframework.data.mongodb.core.mapping.event.AfterSaveEvent;
import org.springframework.data.mongodb.core.mapping.event.BeforeDeleteEvent;
import org.springframework.data.mongodb.core.mapping.event.BeforeSaveEvent;
import org.springframework.data.mongodb.core.mapping.event.MongoMappingEvent;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Only registered if AbstractMongoEventListener is on classpath
 * <p>
 * Provides logging for reactive mongodb transactions with spring data.
 * <p>
 * See: https://docs.spring.io/spring-data/data-mongodb/docs/current/reference/html/#mongodb.mapping-usage.events for documentation of lifecycles
 */
public class MongoDbEventListener extends AbstractMongoEventListener<Object> {
    private final RequestLogger defaultRequestLogger;
    private final RequestLoggingOptions loggingOptions;

    public MongoDbEventListener(RequestLogger defaultRequestLogger) {
        this.defaultRequestLogger = defaultRequestLogger;
        loggingOptions = RequestLoggingOptions.defaults()
                .setLogLevel(Level.DEBUG);
    }

    @Override
    public void onAfterLoad(AfterLoadEvent<Object> event) {
        defaultRequestLogger.logMessage(loggingOptions, getLogMessageMap(Constants.READ, Constants.DB_RESPONSE, event, true));
    }

    @Override
    public void onAfterSave(AfterSaveEvent<Object> event) {
        defaultRequestLogger.logMessage(loggingOptions, getLogMessageMap(Constants.WRITE, Constants.DB_RESPONSE, event, false));
    }

    @Override
    public void onBeforeSave(BeforeSaveEvent<Object> event) {
        defaultRequestLogger.logMessage(loggingOptions, getLogMessageMap(Constants.WRITE, Constants.DB_REQUEST, event, true));
    }

    @Override
    public void onAfterDelete(AfterDeleteEvent<Object> event) {
        // Log db response (delete)
        defaultRequestLogger.logMessage(loggingOptions, getLogMessageMap(Constants.DELETE, Constants.DB_RESPONSE, event, false));
    }

    @Override
    public void onBeforeDelete(BeforeDeleteEvent<Object> event) {
        // Log delete db doc request (delete)
        defaultRequestLogger.logMessage(loggingOptions, getLogMessageMap(Constants.DELETE, Constants.DB_REQUEST, event, true));
    }

    private Map<String, Object> getLogMessageMap(String operation, String type, MongoMappingEvent<?> event, boolean logPayload) {
        final Map<String, Object> map = new LinkedHashMap<>();

        map.put(Constants.DIRECTION, type.equals("Request") ? "Outbound" : "Inbound");

        map.put(Constants.OPERATION, operation); //read/write
        map.put(Constants.TYPE, type); //Request/response
        map.put(Constants.COLLECTION, event.getCollectionName());

        if(logPayload) {
            final Document document = event.getDocument();
            if(document != null) {
                final String json = document.toJson();
                map.put(Constants.PAYLOAD, json);
                map.put(Constants.PAYLOAD_LENGTH, json.length());
            } else {
                map.put(Constants.PAYLOAD_LENGTH, "0");
            }
        }

        return map;
    }
}
