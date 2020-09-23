package org.jitsi.impl.neomedia.device;

import org.jitsi.util.Logger;
import org.jitsi.util.StringUtils;

public class WebrtcAec {
    public static boolean isLoaded;
    private static final Logger logger = Logger.getLogger(WebrtcAec.class);

    static {
        try {
            System.loadLibrary("jnwebrtc");
            System.loadLibrary("jnwebrtcaec");
        } catch (NullPointerException npe) {
            logger.info("Failed to load WebrtcAec library: ", npe);
        } catch (SecurityException se) {
            logger.info("Failed to load WebrtcAec library: ", se);
        } catch (UnsatisfiedLinkError ule) {
            logger.info("Failed to load WebrtcAec library: ", ule);
        }
    }

    public static void init() {
    }

    public static void log(byte[] error) {
        logger.info(StringUtils.newString(error));
    }
}
