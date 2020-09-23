package net.java.sip.communicator.impl.protocol.jabber.extensions;

import java.util.logging.Level;
import java.util.logging.Logger;
import net.java.sip.communicator.impl.protocol.jabber.extensions.AbstractPacketExtension;
import org.jitsi.org.xmlpull.v1.XmlPullParser;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.jivesoftware.smack.provider.ProviderManager;

public class DefaultPacketExtensionProvider<C extends AbstractPacketExtension> implements PacketExtensionProvider {
    private static final Logger logger = Logger.getLogger(DefaultPacketExtensionProvider.class.getName());
    private final Class<C> packetClass;

    public DefaultPacketExtensionProvider(Class<C> c) {
        this.packetClass = c;
    }

    public C parseExtension(XmlPullParser parser) throws Exception {
        AbstractPacketExtension packetExtension = (AbstractPacketExtension) this.packetClass.newInstance();
        int attrCount = parser.getAttributeCount();
        for (int i = 0; i < attrCount; i++) {
            packetExtension.setAttribute(parser.getAttributeName(i), parser.getAttributeValue(i));
        }
        boolean done = false;
        while (!done) {
            int eventType = parser.next();
            String elementName = parser.getName();
            String namespace = parser.getNamespace();
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("Will parse " + elementName + " ns=" + namespace + " class=" + packetExtension.getClass().getSimpleName());
            }
            if (eventType == 2) {
                PacketExtensionProvider provider = (PacketExtensionProvider) ProviderManager.getInstance().getExtensionProvider(elementName, namespace);
                if (provider == null) {
                    logger.fine("Could not add a provider for element " + elementName + " from namespace " + namespace);
                } else {
                    PacketExtension childExtension = provider.parseExtension(parser);
                    if (namespace != null && (childExtension instanceof AbstractPacketExtension)) {
                        ((AbstractPacketExtension) childExtension).setNamespace(namespace);
                    }
                    packetExtension.addChildExtension(childExtension);
                }
            }
            if (eventType == 3 && parser.getName().equals(packetExtension.getElementName())) {
                done = true;
            }
            if (eventType == 4) {
                packetExtension.setText(parser.getText());
            }
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("Done parsing " + elementName);
            }
        }
        return packetExtension;
    }
}
