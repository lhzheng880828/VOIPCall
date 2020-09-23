package net.java.sip.communicator.impl.protocol.jabber.extensions.coin;

import net.java.sip.communicator.impl.protocol.jabber.extensions.DefaultPacketExtensionProvider;
import org.jitsi.org.xmlpull.v1.XmlPullParser;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;

public class EndpointProvider implements PacketExtensionProvider {
    public PacketExtension parseExtension(XmlPullParser parser) throws Exception {
        boolean done = false;
        String entity = parser.getAttributeValue("", "entity");
        StateType state = StateType.full;
        String stateStr = parser.getAttributeValue("", "state");
        if (stateStr != null) {
            state = StateType.parseString(stateStr);
        }
        EndpointPacketExtension ext = new EndpointPacketExtension(entity);
        ext.setAttribute("state", state);
        while (!done) {
            int eventType = parser.next();
            String elementName = parser.getName();
            if (eventType == 2) {
                if (elementName.equals("display-text")) {
                    ext.setDisplayText(CoinIQProvider.parseText(parser));
                } else if (elementName.equals(EndpointPacketExtension.ELEMENT_DISCONNECTION)) {
                    ext.setDisconnectionType(DisconnectionType.parseString(parser.getText()));
                } else if (elementName.equals(EndpointPacketExtension.ELEMENT_JOINING)) {
                    ext.setJoiningType(JoiningType.parseString(CoinIQProvider.parseText(parser)));
                } else if (elementName.equals("status")) {
                    ext.setStatus(EndpointStatusType.parseString(CoinIQProvider.parseText(parser)));
                } else if (elementName.equals(CallInfoPacketExtension.ELEMENT_NAME)) {
                    ext.addChildExtension(new DefaultPacketExtensionProvider(CallInfoPacketExtension.class).parseExtension(parser));
                } else if (elementName.equals("media")) {
                    ext.addChildExtension(new MediaProvider().parseExtension(parser));
                }
            } else if (eventType == 3 && parser.getName().equals("endpoint")) {
                done = true;
            }
        }
        return ext;
    }
}
