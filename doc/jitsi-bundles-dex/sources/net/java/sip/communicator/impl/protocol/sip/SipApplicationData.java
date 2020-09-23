package net.java.sip.communicator.impl.protocol.sip;

import java.util.HashMap;
import java.util.Map;
import net.java.sip.communicator.util.Logger;
import org.jitsi.gov.nist.javax.sip.message.SIPMessage;
import org.jitsi.javax.sip.Dialog;
import org.jitsi.javax.sip.Transaction;

public class SipApplicationData {
    public static final String KEY_SERVICE = "service";
    public static final String KEY_SUBSCRIPTIONS = "subscriptions";
    private static final Logger logger = Logger.getLogger(SipApplicationData.class);
    private final Map<String, Object> storage_ = new HashMap();

    public static void setApplicationData(Object container, String key, Object value) {
        if (container == null) {
            logger.warn("container is null");
        } else if (key == null) {
            logger.warn("key is null");
        } else {
            SipApplicationData appData = getSipApplicationData(container);
            if (appData == null) {
                appData = new SipApplicationData();
                if (container instanceof SIPMessage) {
                    ((SIPMessage) container).setApplicationData(appData);
                } else if (container instanceof Transaction) {
                    ((Transaction) container).setApplicationData(appData);
                } else if (container instanceof Dialog) {
                    ((Dialog) container).setApplicationData(appData);
                } else {
                    logger.error("container should be of type SIPMessage, Transaction or Dialog");
                }
            }
            appData.put(key, value);
        }
    }

    public static Object getApplicationData(Object container, String key) {
        if (container == null) {
            logger.debug("container is null");
            return null;
        } else if (key == null) {
            logger.warn("key is null");
            return null;
        } else {
            SipApplicationData appData = getSipApplicationData(container);
            if (appData != null) {
                return appData.get(key);
            }
            return null;
        }
    }

    private void put(String key, Object value) {
        this.storage_.put(key, value);
    }

    private Object get(String key) {
        return this.storage_.get(key);
    }

    private static SipApplicationData getSipApplicationData(Object container) {
        Object appData;
        if (container instanceof SIPMessage) {
            appData = ((SIPMessage) container).getApplicationData();
        } else if (container instanceof Transaction) {
            appData = ((Transaction) container).getApplicationData();
        } else if (container instanceof Dialog) {
            appData = ((Dialog) container).getApplicationData();
        } else {
            logger.error("container should be of type SIPMessage, Transaction or Dialog");
            appData = null;
        }
        if (appData == null) {
            return null;
        }
        if (appData instanceof SipApplicationData) {
            return (SipApplicationData) appData;
        }
        logger.error("application data should be of type SipApplicationData");
        return null;
    }
}
