package net.java.sip.communicator.impl.protocol.jabber.extensions.jingleinfo;

import org.jitsi.gov.nist.core.Separators;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.PacketExtension;

public class JingleInfoQueryIQ extends IQ {
    public static final String ELEMENT_NAME = "query";
    public static final String NAMESPACE = "google:jingleinfo";

    public String getChildElementXML() {
        StringBuilder bld = new StringBuilder();
        bld.append(Separators.LESS_THAN).append(ELEMENT_NAME).append(" xmlns='").append(NAMESPACE).append(Separators.QUOTE);
        if (getExtensions().size() == 0) {
            bld.append("/>");
        } else {
            bld.append(Separators.GREATER_THAN);
            for (PacketExtension pe : getExtensions()) {
                bld.append(pe.toXML());
            }
            bld.append("</").append(ELEMENT_NAME).append(Separators.GREATER_THAN);
        }
        return bld.toString();
    }
}
