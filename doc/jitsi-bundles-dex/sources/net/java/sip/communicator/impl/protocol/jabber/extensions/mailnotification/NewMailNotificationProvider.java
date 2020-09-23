package net.java.sip.communicator.impl.protocol.jabber.extensions.mailnotification;

import net.java.sip.communicator.util.Logger;
import org.jitsi.org.xmlpull.v1.XmlPullParser;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;

public class NewMailNotificationProvider implements IQProvider {
    private static final Logger logger = Logger.getLogger(NewMailNotificationProvider.class);

    public IQ parseIQ(XmlPullParser parser) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("NewMailNotificationProvider.getChildElementXML usage");
        }
        return new NewMailNotificationIQ();
    }
}
