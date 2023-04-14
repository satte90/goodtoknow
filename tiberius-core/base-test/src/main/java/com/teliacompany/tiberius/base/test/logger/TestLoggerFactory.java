package com.teliacompany.tiberius.base.test.logger;

import java.util.logging.ConsoleHandler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public final class TestLoggerFactory {
    private TestLoggerFactory() {
        //Not to be instantiated
    }

    public static Logger getLogger(Class<?> clazz) {
        Logger mainLogger = Logger.getLogger(clazz.getName());
        mainLogger.setUseParentHandlers(false);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new SimpleFormatter() {
            @Override
            public synchronized String format(LogRecord lr) {
                return String.format("\u001b[34;1m[%1$s]\u001b[0m %2$s %n", lr.getLevel(), lr.getMessage());
            }
        });
        mainLogger.addHandler(handler);

        return Logger.getLogger(clazz.getName());
    }
}
