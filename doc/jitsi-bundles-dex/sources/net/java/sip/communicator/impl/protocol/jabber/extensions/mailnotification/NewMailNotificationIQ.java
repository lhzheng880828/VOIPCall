package net.java.sip.communicator.impl.protocol.jabber.extensions.mailnotification;

import net.java.sip.communicator.util.Logger;
import org.jivesoftware.smack.packet.IQ;

public class NewMailNotificationIQ extends IQ {
    public static final String ELEMENT_NAME = "new-mail";
    public static final String NAMESPACE = "google:mail:notify";
    private static final Logger logger = Logger.getLogger(NewMailNotificationIQ.class);

    public String getChildElementXML() {
        if (logger.isTraceEnabled()) {
            logger.trace("NewMailNotification.getChildElementXML usage");
        }
        return "<iq type='result' from='" + getFrom() + "' " + "to='" + getTo() + "' " + "id='" + getPacketID() + "' />";
    }
}
