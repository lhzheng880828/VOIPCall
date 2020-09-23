package net.java.sip.communicator.impl.protocol.jabber.extensions.notification;

import net.java.sip.communicator.impl.protocol.jabber.extensions.colibri.SourcePacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.ParameterPacketExtension;
import org.jitsi.org.xmlpull.v1.XmlPullParser;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;

public class NotificationEventIQProvider implements IQProvider {
    public IQ parseIQ(XmlPullParser parser) throws Exception {
        NotificationEventIQ result = new NotificationEventIQ();
        boolean done = false;
        while (!done) {
            int eventType = parser.next();
            if (eventType == 2) {
                if (parser.getName().equals("name")) {
                    result.setEventName(parser.nextText());
                } else if (parser.getName().equals(ParameterPacketExtension.VALUE_ATTR_NAME)) {
                    result.setEventValue(parser.nextText());
                } else if (parser.getName().equals(SourcePacketExtension.ELEMENT_NAME)) {
                    result.setEventSource(parser.nextText());
                }
            } else if (eventType == 3 && parser.getName().equals(NotificationEventIQ.ELEMENT_NAME)) {
                done = true;
            }
        }
        return result;
    }
}
