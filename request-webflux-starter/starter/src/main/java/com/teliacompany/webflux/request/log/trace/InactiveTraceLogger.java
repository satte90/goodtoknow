package com.teliacompany.webflux.request.log.trace;

import java.util.Collections;
import java.util.List;

public class InactiveTraceLogger implements TraceLogger {
    @Override
    public void addLogEntry(String messageTemplate, Object... objects) {
        //Do nothing
    }

    @Override
    public void addLogEntry(String messageTemplate, List<Object> ctxObjects, Object... objects) {
        // Do nothing
    }

    @Override
    public List<TraceLogEntry> getLog() {
        return Collections.emptyList();
    }
}
