package org.jitsi.gov.nist.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;
import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.apache.log4j.SimpleLayout;
import org.jitsi.javax.sip.message.Request;

public class LogWriter implements StackLogger {
    private String buildTimeStamp;
    private Properties configurationProperties;
    private int lineCount;
    private String logFileName = null;
    private Logger logger;
    private volatile boolean needsLogging = false;
    private String stackName;
    protected int traceLevel = 0;

    public void logStackTrace() {
        logStackTrace(32);
    }

    public void logStackTrace(int traceLevel) {
        if (this.needsLogging) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            StackTraceElement[] ste = new Exception().getStackTrace();
            for (int i = 1; i < ste.length; i++) {
                pw.print("[" + ste[i].getFileName() + Separators.COLON + ste[i].getLineNumber() + "]");
            }
            pw.close();
            String stackTrace = sw.getBuffer().toString();
            Level level = getLevel(traceLevel);
            if (level.isGreaterOrEqual(getLogPriority())) {
                this.logger.log(level, stackTrace);
            }
        }
    }

    public int getLineCount() {
        return this.lineCount;
    }

    public Logger getLogger() {
        return this.logger;
    }

    public void addAppender(Appender appender) {
        this.logger.addAppender(appender);
    }

    public void logException(Throwable ex) {
        if (this.needsLogging) {
            getLogger().error(ex.getMessage(), ex);
        }
    }

    private void countLines(String message) {
        char[] chars = message.toCharArray();
        for (char c : chars) {
            if (c == 10) {
                this.lineCount++;
            }
        }
    }

    private String enhanceMessage(String message) {
        StackTraceElement elem = new Exception().getStackTrace()[3];
        String className = elem.getClassName();
        String methodName = elem.getMethodName();
        String fileName = elem.getFileName();
        return className + Separators.DOT + methodName + Separators.LPAREN + fileName + Separators.COLON + elem.getLineNumber() + ") [" + message + "]";
    }

    public void logDebug(String message) {
        if (this.needsLogging) {
            String newMessage = enhanceMessage(message);
            if (this.lineCount == 0) {
                getLogger().debug("BUILD TIMESTAMP = " + this.buildTimeStamp);
                getLogger().debug("Config Propeties = " + this.configurationProperties);
            }
            countLines(newMessage);
            getLogger().debug(newMessage);
        }
    }

    public void logTrace(String message) {
        if (this.needsLogging) {
            String newMessage = enhanceMessage(message);
            if (this.lineCount == 0) {
                getLogger().debug("BUILD TIMESTAMP = " + this.buildTimeStamp);
                getLogger().debug("Config Propeties = " + this.configurationProperties);
            }
            countLines(newMessage);
            getLogger().trace(newMessage);
        }
    }

    private void setTraceLevel(int level) {
        this.traceLevel = level;
    }

    public int getTraceLevel() {
        return this.traceLevel;
    }

    public void logFatalError(String message) {
        Logger logger = getLogger();
        String newMsg = enhanceMessage(message);
        countLines(newMsg);
        logger.fatal(newMsg);
    }

    public void logError(String message) {
        Logger logger = getLogger();
        String newMsg = enhanceMessage(message);
        countLines(newMsg);
        logger.error(newMsg);
    }

    public void setStackProperties(Properties configurationProperties) {
        this.configurationProperties = configurationProperties;
        String logLevel = configurationProperties.getProperty("org.jitsi.gov.nist.javax.sip.TRACE_LEVEL");
        this.logFileName = configurationProperties.getProperty("org.jitsi.gov.nist.javax.sip.DEBUG_LOG");
        this.stackName = configurationProperties.getProperty("org.jitsi.javax.sip.STACK_NAME");
        this.logger = Logger.getLogger(configurationProperties.getProperty("org.jitsi.gov.nist.javax.sip.LOG4J_LOGGER_NAME", this.stackName));
        if (logLevel == null) {
            this.needsLogging = false;
        } else if (logLevel.equals("LOG4J")) {
            CommonLogger.useLegacyLogger = false;
        } else {
            try {
                int ll;
                if (logLevel.equals("TRACE")) {
                    ll = 32;
                    Debug.debug = true;
                    Debug.setStackLogger(this);
                } else if (logLevel.equals("DEBUG")) {
                    ll = 32;
                } else if (logLevel.equals(Request.INFO)) {
                    ll = 16;
                } else if (logLevel.equals("ERROR")) {
                    ll = 4;
                } else if (logLevel.equals("NONE") || logLevel.equals("OFF")) {
                    ll = 0;
                } else {
                    ll = Integer.parseInt(logLevel);
                    if (ll > 32) {
                        Debug.debug = true;
                        Debug.setStackLogger(this);
                    }
                }
                setTraceLevel(ll);
                this.needsLogging = true;
                if (this.traceLevel == 32) {
                    this.logger.setLevel(Level.DEBUG);
                } else if (this.traceLevel == 16) {
                    this.logger.setLevel(Level.INFO);
                } else if (this.traceLevel == 4) {
                    this.logger.setLevel(Level.ERROR);
                } else if (this.traceLevel == 0) {
                    this.logger.setLevel(Level.OFF);
                    this.needsLogging = false;
                }
                if (this.needsLogging && this.logFileName != null) {
                    FileAppender fa = null;
                    try {
                        fa = new FileAppender(new SimpleLayout(), this.logFileName, !Boolean.valueOf(configurationProperties.getProperty("org.jitsi.gov.nist.javax.sip.DEBUG_LOG_OVERWRITE")).booleanValue());
                    } catch (FileNotFoundException e) {
                        File logfile = new File(this.logFileName);
                        logfile.getParentFile().mkdirs();
                        logfile.delete();
                        try {
                            fa = new FileAppender(new SimpleLayout(), this.logFileName);
                        } catch (IOException ioe) {
                            ioe.printStackTrace();
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    if (fa != null) {
                        this.logger.addAppender(fa);
                    }
                }
            } catch (NumberFormatException ex2) {
                ex2.printStackTrace();
                System.err.println("LogWriter: Bad integer " + logLevel);
                System.err.println("logging dislabled ");
                this.needsLogging = false;
            }
        }
    }

    public boolean isLoggingEnabled() {
        return this.needsLogging;
    }

    public boolean isLoggingEnabled(int logLevel) {
        return this.needsLogging && logLevel <= this.traceLevel;
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
        this.needsLogging = false;
    }

    public void enableLogging() {
        this.needsLogging = true;
    }

    public void setBuildTimeStamp(String buildTimeStamp) {
        this.buildTimeStamp = buildTimeStamp;
    }

    public Priority getLogPriority() {
        if (this.traceLevel == 16) {
            return Priority.INFO;
        }
        if (this.traceLevel == 4) {
            return Priority.ERROR;
        }
        if (this.traceLevel == 32) {
            return Priority.DEBUG;
        }
        if (this.traceLevel == 64) {
            return Priority.DEBUG;
        }
        return Priority.FATAL;
    }

    public Level getLevel(int traceLevel) {
        if (traceLevel == 16) {
            return Level.INFO;
        }
        if (traceLevel == 4) {
            return Level.ERROR;
        }
        if (traceLevel == 32) {
            return Level.DEBUG;
        }
        if (traceLevel == 64) {
            return Level.ALL;
        }
        return Level.OFF;
    }

    public String getLoggerName() {
        if (this.logger != null) {
            return this.logger.getName();
        }
        return null;
    }
}
