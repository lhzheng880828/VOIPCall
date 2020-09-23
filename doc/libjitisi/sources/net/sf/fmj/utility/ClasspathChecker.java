package net.sf.fmj.utility;

import java.util.logging.Logger;
import javax.media.Manager;
import javax.media.PackageManager;

public final class ClasspathChecker {
    private static final Logger logger = LoggerSingleton.logger;

    public static boolean check() {
        boolean result = true;
        if (!checkFMJPrefixInPackageManager()) {
            result = false;
        }
        if (checkManagerImplementation()) {
            return result;
        }
        return false;
    }

    public static boolean checkAndWarn() {
        boolean result = true;
        if (!checkFMJPrefixInPackageManager()) {
            logger.warning("net.sf.fmj not found in PackageManager.getContentPrefixList() and PackageManager.getProtocolPrefixList(); is JMF ahead of FMJ in the classpath?");
            result = false;
        }
        if (checkJMFInClassPath()) {
            logger.info("JMF detected in classpath");
        }
        if (!checkManagerImplementation()) {
            logger.warning("javax.media.Manager is JMF's implementation, not FMJ's; is JMF ahead of FMJ in the classpath?");
            result = false;
        }
        logger.info("javax.media.Manager version: " + Manager.getVersion());
        return result;
    }

    public static boolean checkFMJPrefixInPackageManager() {
        if (PackageManager.getContentPrefixList().contains("net.sf.fmj") && PackageManager.getProtocolPrefixList().contains("net.sf.fmj")) {
            return true;
        }
        return false;
    }

    public static boolean checkJMFInClassPath() {
        try {
            Class.forName("com.sun.media.BasicClock");
            Class.forName("com.sun.media.BasicCodec");
            Class.forName("com.sun.media.BasicConnector");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean checkManagerImplementation() {
        try {
            Manager.class.getField("FMJ_TAG");
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
