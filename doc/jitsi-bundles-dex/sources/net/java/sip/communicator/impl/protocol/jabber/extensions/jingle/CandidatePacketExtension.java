package net.java.sip.communicator.impl.protocol.jabber.extensions.jingle;

import net.java.sip.communicator.impl.protocol.jabber.extensions.AbstractPacketExtension;

public class CandidatePacketExtension extends AbstractPacketExtension implements Comparable<CandidatePacketExtension> {
    public static final String COMPONENT_ATTR_NAME = "component";
    public static final String ELEMENT_NAME = "candidate";
    public static final String FOUNDATION_ATTR_NAME = "foundation";
    public static final String GENERATION_ATTR_NAME = "generation";
    public static final String ID_ATTR_NAME = "id";
    public static final String IP_ATTR_NAME = "ip";
    public static final String NETWORK_ATTR_NAME = "network";
    public static final String PORT_ATTR_NAME = "port";
    public static final String PRIORITY_ATTR_NAME = "priority";
    public static final String PROTOCOL_ATTR_NAME = "protocol";
    public static final String REL_ADDR_ATTR_NAME = "rel-addr";
    public static final String REL_PORT_ATTR_NAME = "rel-port";
    public static final int RTCP_COMPONENT_ID = 2;
    public static final int RTP_COMPONENT_ID = 1;
    public static final String TYPE_ATTR_NAME = "type";

    public CandidatePacketExtension() {
        super(null, ELEMENT_NAME);
    }

    protected CandidatePacketExtension(String elementName) {
        super(null, elementName);
    }

    public void setComponent(int component) {
        super.setAttribute(COMPONENT_ATTR_NAME, Integer.valueOf(component));
    }

    public int getComponent() {
        return super.getAttributeAsInt(COMPONENT_ATTR_NAME);
    }

    public void setFoundation(String foundation) {
        super.setAttribute(FOUNDATION_ATTR_NAME, foundation);
    }

    public String getFoundation() {
        return super.getAttributeAsString(FOUNDATION_ATTR_NAME);
    }

    public void setGeneration(int generation) {
        super.setAttribute(GENERATION_ATTR_NAME, Integer.valueOf(generation));
    }

    public int getGeneration() {
        return super.getAttributeAsInt(GENERATION_ATTR_NAME);
    }

    public void setID(String id) {
        super.setAttribute("id", id);
    }

    public String getID() {
        return super.getAttributeAsString("id");
    }

    public void setIP(String ip) {
        super.setAttribute(IP_ATTR_NAME, ip);
    }

    public String getIP() {
        return super.getAttributeAsString(IP_ATTR_NAME);
    }

    public void setNetwork(int network) {
        super.setAttribute(NETWORK_ATTR_NAME, Integer.valueOf(network));
    }

    public int getNetwork() {
        return super.getAttributeAsInt(NETWORK_ATTR_NAME);
    }

    public void setPort(int port) {
        super.setAttribute(PORT_ATTR_NAME, Integer.valueOf(port));
    }

    public int getPort() {
        return super.getAttributeAsInt(PORT_ATTR_NAME);
    }

    public void setPriority(long priority) {
        super.setAttribute(PRIORITY_ATTR_NAME, Long.valueOf(priority));
    }

    public int getPriority() {
        return super.getAttributeAsInt(PRIORITY_ATTR_NAME);
    }

    public void setProtocol(String protocol) {
        super.setAttribute(PROTOCOL_ATTR_NAME, protocol);
    }

    public String getProtocol() {
        return super.getAttributeAsString(PROTOCOL_ATTR_NAME);
    }

    public void setRelAddr(String relAddr) {
        super.setAttribute(REL_ADDR_ATTR_NAME, relAddr);
    }

    public String getRelAddr() {
        return super.getAttributeAsString(REL_ADDR_ATTR_NAME);
    }

    public void setRelPort(int relPort) {
        super.setAttribute(REL_PORT_ATTR_NAME, Integer.valueOf(relPort));
    }

    public int getRelPort() {
        return super.getAttributeAsInt(REL_PORT_ATTR_NAME);
    }

    public void setType(CandidateType type) {
        super.setAttribute("type", type);
    }

    public CandidateType getType() {
        return CandidateType.valueOf(getAttributeAsString("type"));
    }

    public int compareTo(CandidatePacketExtension candidatePacketExtension) {
        if (getType() != candidatePacketExtension.getType()) {
            CandidateType[] types = new CandidateType[]{CandidateType.host, CandidateType.local, CandidateType.prflx, CandidateType.srflx, CandidateType.stun, CandidateType.relay};
            for (int i = 0; i < types.length; i++) {
                if (types[i] == getType()) {
                    return -1;
                }
                if (types[i] == candidatePacketExtension.getType()) {
                    return 1;
                }
            }
        }
        return 0;
    }
}
