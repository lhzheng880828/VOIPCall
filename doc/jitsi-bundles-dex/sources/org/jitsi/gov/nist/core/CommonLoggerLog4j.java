package org.jitsi.gov.nist.core;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;
import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class CommonLoggerLog4j implements StackLogger {
    private Logger logger;

    public void logStackTrace() {
        logStackTrace(32);
    }

    public void logStackTrace(int traceLevel) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        StackTraceElement[] ste = new Exception().getStackTrace();
        for (int i = 1; i < ste.length; i++) {
            pw.print("[" + ste[i].getFileName() + Separators.COLON + ste[i].getLineNumber() + "]");
        }
        pw.close();
        this.logger.debug(sw.getBuffer().toString());
    }

    public int getLineCount() {
        return 0;
    }

    public Logger getLogger() {
        return this.logger;
    }

    public void addAppender(Appender appender) {
        this.logger.addAppender(appender);
    }

    public void logException(Throwable ex) {
        this.logger.error("Error", ex);
    }

    public void logDebug(String message) {
        this.logger.debug(message);
    }

    public void logTrace(String message) {
        this.logger.debug(message);
    }

    private void setTraceLevel(int level) {
    }

    public int getTraceLevel() {
        return levelToInt(this.logger.getLevel());
    }

    public void logFatalError(String message) {
        this.logger.fatal(message);
    }

    public void logError(String message) {
        this.logger.error(message);
    }

    public CommonLoggerLog4j(Logger logger) {
        this.logger = logger;
    }

    public void setStackProperties(Properties configurationProperties) {
    }

    public boolean isLoggingEnabled() {
        return this.logger.isInfoEnabled();
    }

    public boolean isLoggingEnabled(int logLevel) {
        return this.logger.isEnabledFor(intToLevel(logLevel));
    }

    public void logError(String message, Exception ex) {
        getLogger().error(message, ex);
    }

    public void logWarning(String string) {
        getLogger().warn(string);
    }

    public void logInfo(String string) {
        getLogger().info(string);
    }

    public void disableLogging() {
    }

    public void enableLogging() {
    }

    public static Level intToLevel(int intLevel) {
        switch (intLevel) {
            case 2:
                return Level.FATAL;
            case 4:
                return Level.ERROR;
            case 8:
                return Level.WARN;
            case 16:
                return Level.INFO;
            case 32:
                return Level.DEBUG;
            case 64:
                return Level.TRACE;
            default:
                return Level.OFF;
        }
    }

    public static int levelToInt(Level level) {
        if (level.equals(Level.INFO)) {
            return 16;
        }
        if (level.equals(Level.ERROR)) {
            return 4;
        }
        if (level.equals(Level.DEBUG)) {
            return 32;
        }
        if (level.equals(Level.WARN)) {
            return 8;
        }
        if (level.equals(Level.TRACE)) {
            return 64;
        }
        if (level.equals(Level.FATAL)) {
            return 2;
        }
        return 0;
    }

    public String getLoggerName() {
        if (this.logger != null) {
            return this.logger.getName();
        }
        return null;
    }

    public void setBuildTimeStamp(String buildTimeStamp) {
        this.logger.info("Build timestamp: " + buildTimeStamp);
    }
}
