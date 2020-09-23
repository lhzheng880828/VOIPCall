package net.java.sip.communicator.impl.protocol.jabber.extensions.carbon;

import net.java.sip.communicator.impl.protocol.jabber.extensions.AbstractPacketExtension;
import org.jitsi.org.xmlpull.v1.XmlPullParser;
import org.jivesoftware.smack.packet.PacketExtension;

public class CarbonPacketExtension extends AbstractPacketExtension {
    public static final String NAMESPACE = "urn:xmpp:carbons:2";
    public static final String PRIVATE_ELEMENT_NAME = "private";
    public static final String RECEIVED_ELEMENT_NAME = "received";
    public static final String SENT_ELEMENT_NAME = "sent";

    public static class PrivateExtension extends AbstractPacketExtension {
        public PrivateExtension() {
            super(CarbonPacketExtension.NAMESPACE, CarbonPacketExtension.PRIVATE_ELEMENT_NAME);
        }
    }

    public static class Provider extends net.java.sip.communicator.impl.protocol.jabber.extensions.carbon.ForwardedPacketExtension.Provider {
        private String elementName;

        public Provider(String elementName) {
            this.elementName = elementName;
        }

        public PacketExtension parseExtension(XmlPullParser parser) throws Exception {
            CarbonPacketExtension packetExtension = new CarbonPacketExtension(this.elementName);
            boolean done = false;
            while (!done) {
                switch (parser.next()) {
                    case 2:
                        if (!ForwardedPacketExtension.ELEMENT_NAME.equals(parser.getName())) {
                            break;
                        }
                        ForwardedPacketExtension extension = (ForwardedPacketExtension) super.parseExtension(parser);
                        if (extension == null) {
                            break;
                        }
                        packetExtension.addChildExtension(extension);
                        break;
                    case 3:
                        if (!this.elementName.equals(parser.getName())) {
                            break;
                        }
                        done = true;
                        break;
                    default:
                        break;
                }
            }
            return packetExtension;
        }
    }

    public CarbonPacketExtension(String elementName) {
        super(NAMESPACE, elementName);
    }
}
