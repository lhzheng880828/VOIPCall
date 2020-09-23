package net.java.sip.communicator.impl.protocol.jabber.extensions.jingleinfo;

import net.java.sip.communicator.impl.protocol.jabber.extensions.DefaultPacketExtensionProvider;
import org.jitsi.org.xmlpull.v1.XmlPullParser;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.jivesoftware.smack.provider.ProviderManager;

public class JingleInfoQueryIQProvider implements IQProvider {
    private final PacketExtensionProvider relayProvider = new RelayProvider();
    private final PacketExtensionProvider stunProvider = new StunProvider();

    public JingleInfoQueryIQProvider() {
        ProviderManager.getInstance().addExtensionProvider(ServerPacketExtension.ELEMENT_NAME, ServerPacketExtension.NAMESPACE, new DefaultPacketExtensionProvider(ServerPacketExtension.class));
    }

    public IQ parseIQ(XmlPullParser parser) throws Exception {
        boolean done = false;
        JingleInfoQueryIQ iq = new JingleInfoQueryIQ();
        while (!done) {
            int eventType = parser.next();
            String elementName = parser.getName();
            if (eventType == 2) {
                if (elementName.equals(StunPacketExtension.ELEMENT_NAME)) {
                    iq.addExtension(this.stunProvider.parseExtension(parser));
                } else if (elementName.equals(RelayPacketExtension.ELEMENT_NAME)) {
                    iq.addExtension(this.relayProvider.parseExtension(parser));
                }
            }
            if (eventType == 3 && parser.getName().equals(JingleInfoQueryIQ.ELEMENT_NAME)) {
                done = true;
            }
        }
        return iq;
    }
}
