package org.jitsi.gov.nist.core;

import java.util.Properties;
import org.apache.log4j.Logger;

public class CommonLogger implements StackLogger {
    public static StackLogger legacyLogger;
    public static boolean useLegacyLogger = true;
    private String name;
    private StackLogger otherLogger;

    public CommonLogger(String name) {
        this.name = name;
    }

    private StackLogger logger() {
        if (useLegacyLogger) {
            return legacyLogger;
        }
        if (this.otherLogger == null) {
            this.otherLogger = new CommonLoggerLog4j(Logger.getLogger(this.name));
        }
        return this.otherLogger;
    }

    public static StackLogger getLogger(String name) {
        return new CommonLogger(name);
    }

    public static StackLogger getLogger(Class clazz) {
        return getLogger(clazz.getName());
    }

    public static void init(Properties p) {
    }

    public void disableLogging() {
        logger().disableLogging();
    }

    public void enableLogging() {
        logger().enableLogging();
    }

    public int getLineCount() {
        return logger().getLineCount();
    }

    public String getLoggerName() {
        return logger().getLoggerName();
    }

    public boolean isLoggingEnabled() {
        return logger().isLoggingEnabled();
    }

    public boolean isLoggingEnabled(int logLevel) {
        return logger().isLoggingEnabled(logLevel);
    }

    public void logDebug(String message) {
        logger().logDebug(message);
    }

    public void logError(String message) {
        logger().logError(message);
    }

    public void logError(String message, Exception ex) {
        logger().logError(message, ex);
    }

    public void logException(Throwable ex) {
        logger().logException(ex);
    }

    public void logFatalError(String message) {
        logger().logFatalError(message);
    }

    public void logInfo(String string) {
        logger().logInfo(string);
    }

    public void logStackTrace() {
        logger().logStackTrace();
    }

    public void logStackTrace(int traceLevel) {
        logger().logStackTrace(traceLevel);
    }

    public void logTrace(String message) {
        logger().logTrace(message);
    }

    public void logWarning(String string) {
        logger().logWarning(string);
    }

    public void setBuildTimeStamp(String buildTimeStamp) {
        logger().setBuildTimeStamp(buildTimeStamp);
    }

    public void setStackProperties(Properties stackProperties) {
        legacyLogger.setStackProperties(stackProperties);
    }
}
