package net.java.sip.communicator.impl.protocol.jabber.extensions.keepalive;

import org.jitsi.org.xmlpull.v1.XmlPullParser;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.provider.IQProvider;

public class KeepAliveEventProvider implements IQProvider {
    public IQ parseIQ(XmlPullParser parser) throws Exception {
        KeepAliveEvent result = new KeepAliveEvent();
        String type = parser.getAttributeValue(null, "type");
        String id = parser.getAttributeValue(null, "id");
        String from = parser.getAttributeValue(null, "from");
        String to = parser.getAttributeValue(null, "to");
        result.setType(Type.fromString(type));
        result.setPacketID(id);
        result.setFrom(from);
        result.setTo(to);
        return result;
    }
}
