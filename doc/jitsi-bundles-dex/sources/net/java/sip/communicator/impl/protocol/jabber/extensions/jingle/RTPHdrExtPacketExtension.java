package net.java.sip.communicator.impl.protocol.jabber.extensions.jingle;

import java.net.URI;
import net.java.sip.communicator.impl.protocol.jabber.extensions.AbstractPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.ContentPacketExtension.SendersEnum;
import org.jivesoftware.smack.packet.PacketExtension;

public class RTPHdrExtPacketExtension extends AbstractPacketExtension {
    public static final String ATTRIBUTES_ATTR_NAME = "attributes";
    public static final String ELEMENT_NAME = "rtp-hdrext";
    public static final String ID_ATTR_NAME = "id";
    public static final String NAMESPACE = "urn:xmpp:jingle:apps:rtp:rtp-hdrext:0";
    public static final String SENDERS_ATTR_NAME = "senders";
    public static final String URI_ATTR_NAME = "uri";

    public RTPHdrExtPacketExtension() {
        super("urn:xmpp:jingle:apps:rtp:rtp-hdrext:0", ELEMENT_NAME);
    }

    public void setID(String id) {
        setAttribute("id", id);
    }

    public String getID() {
        return getAttributeAsString("id");
    }

    public void setSenders(SendersEnum senders) {
        setAttribute("senders", senders);
    }

    public SendersEnum getSenders() {
        String attributeVal = getAttributeAsString("senders");
        return attributeVal == null ? null : SendersEnum.valueOf(attributeVal.toString());
    }

    public void setURI(URI uri) {
        setAttribute("uri", uri.toString());
    }

    public URI getURI() {
        return getAttributeAsURI("uri");
    }

    public void setAttributes(String attributes) {
        ParameterPacketExtension paramExt = new ParameterPacketExtension();
        paramExt.setName(ATTRIBUTES_ATTR_NAME);
        paramExt.setValue(attributes);
        addChildExtension(paramExt);
    }

    public String getAttributes() {
        for (PacketExtension ext : getChildExtensions()) {
            if (ext instanceof ParameterPacketExtension) {
                ParameterPacketExtension p = (ParameterPacketExtension) ext;
                if (p.getName().equals(ATTRIBUTES_ATTR_NAME)) {
                    return p.getValue();
                }
            }
        }
        return null;
    }
}
