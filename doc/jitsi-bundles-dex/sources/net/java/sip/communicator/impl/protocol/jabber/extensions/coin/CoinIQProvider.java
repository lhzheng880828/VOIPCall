package net.java.sip.communicator.impl.protocol.jabber.extensions.coin;

import net.java.sip.communicator.impl.protocol.jabber.extensions.DefaultPacketExtensionProvider;
import org.jitsi.org.xmlpull.v1.XmlPullParser;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.jivesoftware.smack.provider.ProviderManager;

public class CoinIQProvider implements IQProvider {
    private final PacketExtensionProvider descriptionProvider = new DescriptionProvider();
    private final DefaultPacketExtensionProvider<SidebarsByValPacketExtension> sidebarsByValProvider = new DefaultPacketExtensionProvider(SidebarsByValPacketExtension.class);
    private final StateProvider stateProvider = new StateProvider();
    private final DefaultPacketExtensionProvider<URIsPacketExtension> urisProvider = new DefaultPacketExtensionProvider(URIsPacketExtension.class);
    private final PacketExtensionProvider usersProvider = new UsersProvider();

    public CoinIQProvider() {
        ProviderManager providerManager = ProviderManager.getInstance();
        providerManager.addExtensionProvider(UserRolesPacketExtension.ELEMENT_NAME, "", new DefaultPacketExtensionProvider(UserRolesPacketExtension.class));
        providerManager.addExtensionProvider("uri", "", new DefaultPacketExtensionProvider(URIPacketExtension.class));
        providerManager.addExtensionProvider("sip", "", new DefaultPacketExtensionProvider(SIPDialogIDPacketExtension.class));
        providerManager.addExtensionProvider(ConferenceMediumPacketExtension.ELEMENT_NAME, "", new ConferenceMediumProvider());
        providerManager.addExtensionProvider(ConferenceMediaPacketExtension.ELEMENT_NAME, "", new DefaultPacketExtensionProvider(ConferenceMediaPacketExtension.class));
        providerManager.addExtensionProvider(CallInfoPacketExtension.ELEMENT_NAME, "", new DefaultPacketExtensionProvider(CallInfoPacketExtension.class));
    }

    public IQ parseIQ(XmlPullParser parser) throws Exception {
        CoinIQ coinIQ = new CoinIQ();
        String entity = parser.getAttributeValue("", "entity");
        String version = parser.getAttributeValue("", "version");
        StateType state = StateType.full;
        String stateStr = parser.getAttributeValue("", "state");
        String sid = parser.getAttributeValue("", "sid");
        if (stateStr != null) {
            state = StateType.parseString(stateStr);
        }
        coinIQ.setEntity(entity);
        coinIQ.setVersion(Integer.parseInt(version));
        coinIQ.setState(state);
        coinIQ.setSID(sid);
        boolean done = false;
        while (!done) {
            int eventType = parser.next();
            String elementName = parser.getName();
            if (eventType == 2) {
                if (elementName.equals(DescriptionPacketExtension.ELEMENT_NAME)) {
                    coinIQ.addExtension(this.descriptionProvider.parseExtension(parser));
                } else if (elementName.equals(UsersPacketExtension.ELEMENT_NAME)) {
                    coinIQ.addExtension(this.usersProvider.parseExtension(parser));
                } else if (elementName.equals(StatePacketExtension.ELEMENT_NAME)) {
                    coinIQ.addExtension(this.stateProvider.parseExtension(parser));
                } else if (elementName.equals(URIsPacketExtension.ELEMENT_NAME)) {
                    coinIQ.addExtension(this.urisProvider.parseExtension(parser));
                } else if (elementName.equals(SidebarsByValPacketExtension.ELEMENT_NAME)) {
                    coinIQ.addExtension(this.sidebarsByValProvider.parseExtension(parser));
                }
            }
            if (eventType == 3 && parser.getName().equals("conference-info")) {
                done = true;
            }
        }
        return coinIQ;
    }

    public static String parseText(XmlPullParser parser) throws Exception {
        boolean done = false;
        String text = null;
        while (!done) {
            int eventType = parser.next();
            if (eventType == 4) {
                text = parser.getText();
            } else if (eventType == 3) {
                done = true;
            }
        }
        return text;
    }
}
