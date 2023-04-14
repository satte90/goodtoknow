package com.teliacompany.tiberius.base.businessrules.preprocessor.logger;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public final class LoggerFactory {
    private static Logger logoLogger;
    private static Logger mainLogger;

    static {
        Logger mainLogger = Logger.getLogger("com.teliacompany.tiberius.base.businessrules.preprocessor.main");
        mainLogger.setUseParentHandlers(false);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new SimpleFormatter() {
            private static final String FORMAT = "[\u001b[1;%1$dm%2$s\u001b[0m] %3$s %n";

            @Override
            public synchronized String format(LogRecord lr) {
                int levelColor = getLevelColor(lr.getLevel());
                return String.format(FORMAT, levelColor, getLevelName(lr.getLevel()), lr.getMessage());
            }
        });
        mainLogger.addHandler(handler);
        LoggerFactory.mainLogger = Logger.getLogger("com.teliacompany.tiberius.base.businessrules.preprocessor.main");

        Logger logoLogger = Logger.getLogger("com.teliacompany.tiberius.base.businessrules.preprocessor.logo");
        logoLogger.setUseParentHandlers(false);
        ConsoleHandler logoHandler = new ConsoleHandler();
        logoHandler.setFormatter(new SimpleFormatter() {
            private static final String FORMAT = "%1$s %n";

            @Override
            public synchronized String format(LogRecord lr) {
                return String.format(FORMAT, lr.getMessage());
            }
        });
        logoLogger.addHandler(handler);
        LoggerFactory.logoLogger = Logger.getLogger("com.teliacompany.tiberius.base.businessrules.preprocessor.logo");
    }

    private static int getLevelColor(Level level) {
        switch(level.intValue()) {
            case 1000:
                return 31;
            case 900:
                return 33;
            case 500:
            case 400:
            case 300:
                return 37;
            case 800:
            case 700:
            default:
                return 94;
        }
    }

    private static String getLevelName(Level level) {
        switch(level.intValue()) {
            case 1000:
                return "ERROR";
            case 900:
                return "WARNING";
            case 500:
            case 400:
            case 300:
                return "DEBUG";
            case 800:
            case 700:
            default:
                return "INFO";
        }
    }

    private LoggerFactory() {
        //Not to be instantiated
    }

    public static Logger getLogoLogger() {
        return logoLogger;
    }

    public static Logger getMainLogger() {
        return mainLogger;
    }
}
