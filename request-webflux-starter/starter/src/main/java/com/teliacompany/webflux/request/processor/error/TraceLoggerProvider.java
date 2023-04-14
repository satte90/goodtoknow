package com.teliacompany.webflux.request.processor.error;

import com.teliacompany.webflux.error.api.ErrorAttribute;
import com.teliacompany.webflux.request.context.ContextWrapper;
import com.teliacompany.webflux.request.log.trace.TraceLogEntry;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TraceLoggerProvider implements ErrorAttributesProvider {
    @Override
    public Map<String, Object> getErrorAttributes(ReadOnlyWebException webException, ContextWrapper contextData) {
        if(contextData.getTransactionContext().isTracingEnabled()) {
            var log = contextData.getTraceLogger().getLog();
            return Map.of(ErrorAttribute.TRACE_LOG, getTimedLogEntries(log));
        }
        return Map.of();
    }

    public String getTraceLogJson(List<TraceLogEntry> log) {
        List<String> list = getTimedLogEntries(log);

        StringBuilder logJsonBuilder = new StringBuilder();
        logJsonBuilder.append("[");
        list.forEach(s -> logJsonBuilder.append("\"").append(s).append("\","));
        logJsonBuilder.deleteCharAt(logJsonBuilder.length() - 1); //Remove last comma
        logJsonBuilder.append("]");
        return logJsonBuilder.toString();
    }

    private static List<String> getTimedLogEntries(List<TraceLogEntry> log) {
        Instant startTime = log.stream().findFirst().map(TraceLogEntry::getTimestamp).orElse(Instant.now());
        return log.stream().map(e -> {
            long ms = e.getTimestamp().toEpochMilli() - startTime.toEpochMilli();
            String msg = e.getMessage().replaceAll("\\n\\s*", " ");
            return ms + " ms: " + msg;
        }).collect(Collectors.toList());
    }
}
