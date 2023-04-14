package com.teliacompany.webflux.request.log.trace;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ActiveTraceLogger implements TraceLogger {
    public static final String TMP_CURLY_BRACKETS = "!<<[]>>";
    private final List<TraceLogEntry> logEntries = new ArrayList<>();

    @Override
    public void addLogEntry(String messageTemplate, List<Object> ctxObjects, Object... objects) {
        String message = messageTemplate;
        for(Object object : ctxObjects) {
            String objectString = object.toString().replace("{}", TMP_CURLY_BRACKETS);
            message = message.replaceFirst("\\{}", objectString);
        }
        addLogEntry(message, objects);
    }

    @Override
    public void addLogEntry(String messageTemplate, Object... objects) {
        String message = messageTemplate;
        for(Object object : objects) {
            object = object == null ? "null" : object;
            String objectString = object.toString().replace("{}", TMP_CURLY_BRACKETS);
            message = message.replaceFirst("\\{}", objectString);
        }
        message = message.replace(TMP_CURLY_BRACKETS, "{}");
        logEntries.add(new TraceLogEntry(Instant.now(), message));
    }

    public List<TraceLogEntry> getLog() {
        return logEntries;
    }
}
