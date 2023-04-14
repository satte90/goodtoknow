package com.teliacompany.webflux.request.log.trace;

import java.time.Instant;

public class TraceLogEntry {
    private final Instant timestamp;
    private final String message;

    public TraceLogEntry(Instant timestamp, String message) {
        this.timestamp = timestamp;
        this.message = message;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }
}
