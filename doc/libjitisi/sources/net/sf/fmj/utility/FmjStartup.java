package net.sf.fmj.utility;

import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import net.sf.fmj.media.RegistryDefaults;
import org.jitsi.android.util.javax.swing.UIManager;

public class FmjStartup {
    private static boolean initialized = false;
    public static boolean isApplet = false;
    private static final Logger logger = LoggerSingleton.logger;

    public static final void init() {
        if (!initialized) {
            logger.info("OS: " + System.getProperty("os.name"));
            System.setProperty("java.util.logging.config.file", "logging.properties");
            try {
                LogManager.getLogManager().readConfiguration();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Unable to read logging configuration: " + e, e);
                System.err.println("Unable to read logging configuration: " + e);
                e.printStackTrace();
            }
            if (!ClasspathChecker.checkAndWarn()) {
                logger.info("Enabling JMF logging");
                if (!JmfUtility.enableLogging()) {
                    logger.warning("Failed to enable JMF logging");
                }
                logger.info("Registering FMJ prefixes and plugins with JMF");
                RegistryDefaults.registerAll(10);
            }
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e2) {
                logger.log(Level.WARNING, "" + e2, e2);
            }
            initialized = true;
        }
    }

    public static final void initApplet() {
        if (!initialized) {
            if (!ClasspathChecker.checkAndWarn()) {
                logger.info("Enabling JMF logging");
                if (!JmfUtility.enableLogging()) {
                    logger.warning("Failed to enable JMF logging");
                }
                logger.info("Registering FMJ prefixes and plugins with JMF");
                RegistryDefaults.registerAll(2);
            }
            initialized = true;
        }
    }
}
