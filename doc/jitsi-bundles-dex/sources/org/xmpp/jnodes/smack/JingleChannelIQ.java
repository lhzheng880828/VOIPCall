package org.xmpp.jnodes.smack;

import org.jitsi.gov.nist.core.Separators;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.packet.Packet;

public class JingleChannelIQ extends IQ {
    public static final String NAME = "channel";
    public static final String NAMESPACE = "http://jabber.org/protocol/jinglenodes#channel";
    public static final String TCP = "tcp";
    public static final String UDP = "udp";
    private String host;
    private String id;
    private int localport = -1;
    private String protocol = "udp";
    private int remoteport = -1;

    public JingleChannelIQ() {
        setType(Type.GET);
        setPacketID(Packet.nextID());
    }

    public String getChildElementXML() {
        StringBuilder str = new StringBuilder();
        str.append(Separators.LESS_THAN).append("channel").append(" xmlns='").append(NAMESPACE).append("' protocol='").append(this.protocol).append("' ");
        if (this.localport > 0 && this.remoteport > 0 && this.host != null) {
            str.append("host='").append(this.host).append("' ");
            str.append("localport='").append(this.localport).append("' ");
            str.append("remoteport='").append(this.remoteport).append("' ");
        }
        str.append("/>");
        return str.toString();
    }

    public boolean isRequest() {
        return Type.GET.equals(getType());
    }

    public String getProtocol() {
        return this.protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public int getRemoteport() {
        return this.remoteport;
    }

    public void setRemoteport(int remoteport) {
        this.remoteport = remoteport;
    }

    public String getHost() {
        return this.host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getLocalport() {
        return this.localport;
    }

    public void setLocalport(int localport) {
        this.localport = localport;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public static IQ createEmptyResult(IQ iq) {
        return createIQ(iq.getPacketID(), iq.getFrom(), iq.getTo(), Type.RESULT);
    }

    public static IQ createEmptyError(IQ iq) {
        return createIQ(iq.getPacketID(), iq.getFrom(), iq.getTo(), Type.ERROR);
    }

    public static IQ createEmptyError() {
        return createIQ(null, null, null, Type.ERROR);
    }

    public static IQ createIQ(String ID, String to, String from, Type type) {
        IQ iqPacket = new IQ() {
            public String getChildElementXML() {
                return null;
            }
        };
        iqPacket.setPacketID(ID);
        iqPacket.setTo(to);
        iqPacket.setFrom(from);
        iqPacket.setType(type);
        return iqPacket;
    }
}
