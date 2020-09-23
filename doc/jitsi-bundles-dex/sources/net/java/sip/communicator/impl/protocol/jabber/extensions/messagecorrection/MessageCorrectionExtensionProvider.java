package net.java.sip.communicator.impl.protocol.jabber.extensions.messagecorrection;

import org.jitsi.org.xmlpull.v1.XmlPullParser;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;

public class MessageCorrectionExtensionProvider implements PacketExtensionProvider {
    public PacketExtension parseExtension(XmlPullParser parser) throws Exception {
        MessageCorrectionExtension res = new MessageCorrectionExtension(null);
        do {
            if (parser.getEventType() == 2) {
                res.setCorrectedMessageUID(parser.getAttributeValue(null, "id"));
            }
        } while (parser.next() != 3);
        return res;
    }
}
