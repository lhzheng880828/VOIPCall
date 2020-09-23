package net.java.sip.communicator.impl.protocol.jabber.extensions.coin;

import org.jitsi.gov.nist.core.Separators;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.PacketExtension;

public class CoinIQ extends IQ {
    public static final String ELEMENT_NAME = "conference-info";
    public static final String ENTITY_ATTR_NAME = "entity";
    public static final String NAMESPACE = "urn:ietf:params:xml:ns:conference-info";
    public static final String SID_ATTR_NAME = "sid";
    public static final String STATE_ATTR_NAME = "state";
    public static final String VERSION_ATTR_NAME = "version";
    private String entity = null;
    private String sid = null;
    private StateType state = StateType.full;
    private int version = 0;

    public String getChildElementXML() {
        StringBuilder bldr = new StringBuilder(Separators.LESS_THAN);
        bldr.append("conference-info");
        bldr.append(" xmlns='").append(NAMESPACE).append(Separators.QUOTE);
        bldr.append(" state='").append(this.state).append(Separators.QUOTE);
        bldr.append(" entity='").append(this.entity).append(Separators.QUOTE);
        bldr.append(" version='").append(this.version).append(Separators.QUOTE);
        if (this.sid != null) {
            bldr.append(" sid='").append(this.sid).append(Separators.QUOTE);
        }
        if (getExtensions().size() == 0) {
            bldr.append("/>");
        } else {
            bldr.append(Separators.GREATER_THAN);
            for (PacketExtension pe : getExtensions()) {
                bldr.append(pe.toXML());
            }
            bldr.append("</").append("conference-info").append(Separators.GREATER_THAN);
        }
        return bldr.toString();
    }

    public String getEntity() {
        return this.entity;
    }

    public String getSID() {
        return this.sid;
    }

    public StateType getState() {
        return this.state;
    }

    public int getVersion() {
        return this.version;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public void setSID(String sid) {
        this.sid = sid;
    }

    public void setState(StateType state) {
        this.state = state;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}
