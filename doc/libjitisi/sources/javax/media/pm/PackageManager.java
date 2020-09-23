package javax.media.pm;

import java.io.IOException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.fmj.registry.Registry;
import net.sf.fmj.utility.LoggerSingleton;

public final class PackageManager extends javax.media.PackageManager {
    private static final Logger logger = LoggerSingleton.logger;
    private static Registry registry = Registry.getInstance();

    public static synchronized void commitContentPrefixList() {
        synchronized (PackageManager.class) {
            try {
                registry.commit();
            } catch (Exception e) {
                logger.log(Level.WARNING, "" + e, e);
            }
        }
        return;
    }

    public static synchronized void commitProtocolPrefixList() {
        synchronized (PackageManager.class) {
            try {
                registry.commit();
            } catch (IOException e) {
                logger.log(Level.WARNING, "" + e, e);
            }
        }
        return;
    }

    public static synchronized Vector<String> getContentPrefixList() {
        Vector contentPrefixList;
        synchronized (PackageManager.class) {
            contentPrefixList = registry.getContentPrefixList();
        }
        return contentPrefixList;
    }

    public static synchronized Vector<String> getProtocolPrefixList() {
        Vector protocolPrefixList;
        synchronized (PackageManager.class) {
            protocolPrefixList = registry.getProtocolPrefixList();
        }
        return protocolPrefixList;
    }

    public static synchronized void setContentPrefixList(Vector list) {
        synchronized (PackageManager.class) {
            registry.setContentPrefixList(list);
        }
    }

    public static synchronized void setProtocolPrefixList(Vector list) {
        synchronized (PackageManager.class) {
            registry.setProtocolPrefixList(list);
        }
    }
}
