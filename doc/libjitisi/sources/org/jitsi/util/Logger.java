package org.jitsi.util;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;

public class Logger {
    private final java.util.logging.Logger loggerDelegate;

    private Logger(java.util.logging.Logger logger) {
        this.loggerDelegate = logger;
    }

    public static Logger getLogger(Class<?> clazz) throws NullPointerException {
        return getLogger(clazz.getName());
    }

    public static Logger getLogger(String name) throws NullPointerException {
        return new Logger(java.util.logging.Logger.getLogger(name));
    }

    public void logEntry() {
        if (this.loggerDelegate.isLoggable(Level.FINEST)) {
            this.loggerDelegate.log(Level.FINEST, "[entry] " + new Throwable().getStackTrace()[1].getMethodName());
        }
    }

    public void logExit() {
        if (this.loggerDelegate.isLoggable(Level.FINEST)) {
            this.loggerDelegate.log(Level.FINEST, "[exit] " + new Throwable().getStackTrace()[1].getMethodName());
        }
    }

    public boolean isTraceEnabled() {
        return this.loggerDelegate.isLoggable(Level.FINER);
    }

    public void trace(Object msg) {
        this.loggerDelegate.finer(msg != null ? msg.toString() : "null");
    }

    public void trace(Object msg, Throwable t) {
        this.loggerDelegate.log(Level.FINER, msg != null ? msg.toString() : "null", t);
    }

    public boolean isDebugEnabled() {
        return this.loggerDelegate.isLoggable(Level.FINE);
    }

    public void debug(Object msg) {
        this.loggerDelegate.fine(msg != null ? msg.toString() : "null");
    }

    public void debug(Object msg, Throwable t) {
        this.loggerDelegate.log(Level.FINE, msg != null ? msg.toString() : "null", t);
    }

    public boolean isInfoEnabled() {
        return this.loggerDelegate.isLoggable(Level.INFO);
    }

    public void info(Object msg) {
        this.loggerDelegate.info(msg != null ? msg.toString() : "null");
    }

    public void info(Object msg, Throwable t) {
        this.loggerDelegate.log(Level.INFO, msg != null ? msg.toString() : "null", t);
    }

    public void warn(Object msg) {
        this.loggerDelegate.warning(msg != null ? msg.toString() : "null");
    }

    public void warn(Object msg, Throwable t) {
        this.loggerDelegate.log(Level.WARNING, msg != null ? msg.toString() : "null", t);
    }

    public void error(Object msg) {
        this.loggerDelegate.severe(msg != null ? msg.toString() : "null");
    }

    public void error(Object msg, Throwable t) {
        this.loggerDelegate.log(Level.SEVERE, msg != null ? msg.toString() : "null", t);
    }

    public void fatal(Object msg) {
        this.loggerDelegate.severe(msg != null ? msg.toString() : "null");
    }

    public void fatal(Object msg, Throwable t) {
        this.loggerDelegate.log(Level.SEVERE, msg != null ? msg.toString() : "null", t);
    }

    public void setLevelFatal() {
        setLevel(Level.SEVERE);
    }

    public void setLevelError() {
        setLevel(Level.SEVERE);
    }

    public void setLevelWarn() {
        setLevel(Level.WARNING);
    }

    public void setLevelInfo() {
        setLevel(Level.INFO);
    }

    public void setLevelDebug() {
        setLevel(Level.FINE);
    }

    public void setLevelTrace() {
        setLevel(Level.FINER);
    }

    public void setLevelAll() {
        setLevel(Level.ALL);
    }

    public void setLevelOff() {
        setLevel(Level.OFF);
    }

    private void setLevel(Level level) {
        for (Handler handler : this.loggerDelegate.getHandlers()) {
            handler.setLevel(level);
        }
        this.loggerDelegate.setLevel(level);
    }

    public void reset() {
        try {
            FileHandler.pattern = null;
            LogManager.getLogManager().reset();
            LogManager.getLogManager().readConfiguration();
        } catch (Exception e) {
            this.loggerDelegate.log(Level.INFO, "Failed to reinit logger.", e);
        }
    }
}
