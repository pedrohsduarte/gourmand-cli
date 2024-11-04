package tech.pedroduarte.gourmand.common.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.LoggerFactory;

public class LoggingConfig {

    public static void configureLogging(String[] args) {

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);

        boolean enableLogging = shouldEnableLogging(args);

        if (enableLogging) {
            rootLogger.setLevel(Level.INFO);
        } else {
            rootLogger.setLevel(Level.OFF);
        }
    }

    private static boolean shouldEnableLogging(String[] args) {
        for (String arg : args) {
            if (arg.equals("--verbose") || arg.equals("-v")) {
                return true;
            }
        }
        return false;
    }
}
