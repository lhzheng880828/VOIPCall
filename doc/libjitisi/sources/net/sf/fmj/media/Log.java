package net.sf.fmj.media;

import com.sun.media.util.Registry;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Log {
    private static int indent = 0;
    public static final boolean isEnabled;
    private static Logger logger = Logger.getLogger(Log.class.getName());

    static {
        boolean z = false;
        Object allowLogging = Registry.get("allowLogging");
        if (allowLogging != null && (allowLogging instanceof Boolean)) {
            z = ((Boolean) allowLogging).booleanValue();
        }
        isEnabled = z;
        if (isEnabled) {
            writeHeader();
        }
    }

    public static synchronized void comment(Object str) {
        synchronized (Log.class) {
            if (isEnabled && logger.isLoggable(Level.FINE)) {
                logger.fine(str != null ? str.toString() : "null");
            }
        }
    }

    public static synchronized void info(Object str) {
        synchronized (Log.class) {
            if (isEnabled && logger.isLoggable(Level.INFO)) {
                logger.info(str != null ? str.toString() : "null");
            }
        }
    }

    public static synchronized void decrIndent() {
        synchronized (Log.class) {
            indent--;
        }
    }

    public static synchronized void dumpStack(Throwable e) {
        synchronized (Log.class) {
            if (isEnabled && logger.isLoggable(Level.FINE)) {
                for (StackTraceElement s : e.getStackTrace()) {
                    logger.fine(s.toString());
                }
            }
        }
    }

    public static synchronized void error(Object str) {
        synchronized (Log.class) {
            if (isEnabled && logger.isLoggable(Level.SEVERE)) {
                logger.severe(str != null ? str.toString() : "null");
            } else {
                System.err.println(str);
            }
        }
    }

    public static int getIndent() {
        return indent;
    }

    public static synchronized void incrIndent() {
        synchronized (Log.class) {
            indent++;
        }
    }

    public static synchronized void profile(Object str) {
        synchronized (Log.class) {
            if (isEnabled && logger.isLoggable(Level.FINER)) {
                logger.finer(str != null ? str.toString() : "null");
            }
        }
    }

    public static synchronized void setIndent(int i) {
        synchronized (Log.class) {
            indent = i;
        }
    }

    public static synchronized void warning(Object str) {
        synchronized (Log.class) {
            if (isEnabled && logger.isLoggable(Level.WARNING)) {
                logger.warning(str != null ? str.toString() : "null");
            }
        }
    }

    public static synchronized void write(Object str) {
        synchronized (Log.class) {
            if (isEnabled && logger.isLoggable(Level.FINE)) {
                StringBuilder sb = new StringBuilder();
                for (int i = indent; i > 0; i--) {
                    sb.append("    ");
                }
                sb.append(str != null ? str.toString() : "null");
                logger.fine(sb.toString());
            }
        }
    }

    private static synchronized void writeHeader() {
        synchronized (Log.class) {
            write("#\n# FMJ\n#\n");
            try {
                String os = System.getProperty("os.name");
                String osarch = System.getProperty("os.arch");
                String osver = System.getProperty("os.version");
                String java = System.getProperty("java.vendor");
                String jver = System.getProperty("java.version");
                if (os != null) {
                    comment("Platform: " + os + ", " + osarch + ", " + osver);
                }
                if (java != null) {
                    comment("Java VM: " + java + ", " + jver);
                }
                write("");
            } catch (Throwable th) {
            }
        }
    }
}
