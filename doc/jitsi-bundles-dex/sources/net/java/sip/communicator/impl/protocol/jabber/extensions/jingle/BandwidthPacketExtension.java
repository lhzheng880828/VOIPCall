package net.java.sip.communicator.impl.protocol.jabber.extensions.jingle;

import net.java.sip.communicator.impl.protocol.jabber.extensions.AbstractPacketExtension;

public class BandwidthPacketExtension extends AbstractPacketExtension {
    public static final String ELEMENT_NAME = "bandwidth";
    public static final String TYPE_ATTR_NAME = "type";

    public BandwidthPacketExtension() {
        super(null, ELEMENT_NAME);
    }

    public void setType(String type) {
        setAttribute("type", type);
    }

    public String getType() {
        return getAttributeAsString("type");
    }

    public void setBandwidth(String bw) {
        super.setText(bw);
    }

    public String getBandwidth() {
        return super.getText();
    }
}
