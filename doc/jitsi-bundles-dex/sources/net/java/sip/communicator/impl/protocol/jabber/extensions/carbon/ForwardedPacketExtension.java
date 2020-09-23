package net.java.sip.communicator.impl.protocol.jabber.extensions.carbon;

import net.java.sip.communicator.impl.protocol.jabber.extensions.AbstractPacketExtension;
import org.jitsi.org.xmlpull.v1.XmlPullParser;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.jivesoftware.smack.util.PacketParserUtils;

public class ForwardedPacketExtension extends AbstractPacketExtension {
    public static final String ELEMENT_NAME = "forwarded";
    public static final String NAMESPACE = "urn:xmpp:forward:0";
    private Message message = null;

    public static class Provider implements PacketExtensionProvider {
        public PacketExtension parseExtension(XmlPullParser parser) throws Exception {
            ForwardedPacketExtension packetExtension = new ForwardedPacketExtension();
            boolean done = false;
            while (!done) {
                switch (parser.next()) {
                    case 2:
                        if (!"message".equals(parser.getName())) {
                            break;
                        }
                        Message message = (Message) PacketParserUtils.parseMessage(parser);
                        if (message == null) {
                            break;
                        }
                        packetExtension.setMessage(message);
                        break;
                    case 3:
                        if (!ForwardedPacketExtension.ELEMENT_NAME.equals(parser.getName())) {
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

    public ForwardedPacketExtension() {
        super(NAMESPACE, ELEMENT_NAME);
    }

    public void setMessage(Message message) {
        this.message = message;
        addPacket(message);
    }

    public Message getMessage() {
        return this.message;
    }
}
