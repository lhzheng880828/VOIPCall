package net.java.sip.communicator.impl.protocol.jabber.extensions.colibri;

import java.util.List;
import net.java.sip.communicator.impl.protocol.jabber.extensions.AbstractPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.ParameterPacketExtension;

public class SourcePacketExtension extends AbstractPacketExtension {
    public static final String ELEMENT_NAME = "source";
    public static final String NAMESPACE = "urn:xmpp:jingle:apps:rtp:ssma:0";
    private static final String SSRC_ATTR_NAME = "ssrc";

    public SourcePacketExtension() {
        super(NAMESPACE, ELEMENT_NAME);
    }

    public void addParameter(ParameterPacketExtension parameter) {
        addChildExtension(parameter);
    }

    public List<ParameterPacketExtension> getParameters() {
        return getChildExtensionsOfType(ParameterPacketExtension.class);
    }

    public long getSSRC() {
        String s = getAttributeAsString("ssrc");
        return s == null ? -1 : Long.parseLong(s);
    }

    public void setSSRC(long ssrc) {
        if (ssrc == -1) {
            removeAttribute("ssrc");
        } else {
            setAttribute("ssrc", Long.toString(ssrc));
        }
    }
}
