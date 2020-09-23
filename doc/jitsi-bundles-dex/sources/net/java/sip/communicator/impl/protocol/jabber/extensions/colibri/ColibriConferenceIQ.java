package net.java.sip.communicator.impl.protocol.jabber.extensions.colibri;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.IceUdpTransportPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.ParameterPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.PayloadTypePacketExtension;
import org.jitsi.service.neomedia.MediaDirection;
import org.jivesoftware.smack.packet.IQ;

public class ColibriConferenceIQ extends IQ {
    public static final String ELEMENT_NAME = "conference";
    public static final String ID_ATTR_NAME = "id";
    public static final String NAMESPACE = "http://jitsi.org/protocol/colibri";
    public static final int[] NO_SSRCS = new int[0];
    private final List<Content> contents = new LinkedList();
    private String id;

    public static class Channel {
        public static final String DIRECTION_ATTR_NAME = "direction";
        public static final String ELEMENT_NAME = "channel";
        public static final String ENDPOINT_ATTR_NAME = "endpoint";
        public static final String EXPIRE_ATTR_NAME = "expire";
        public static final int EXPIRE_NOT_SPECIFIED = -1;
        @Deprecated
        public static final String HOST_ATTR_NAME = "host";
        public static final String ID_ATTR_NAME = "id";
        public static final String INITIATOR_ATTR_NAME = "initiator";
        public static final String LAST_N_ATTR_NAME = "last-n";
        @Deprecated
        public static final String RTCP_PORT_ATTR_NAME = "rtcpport";
        public static final String RTP_LEVEL_RELAY_TYPE_ATTR_NAME = "rtp-level-relay-type";
        @Deprecated
        public static final String RTP_PORT_ATTR_NAME = "rtpport";
        public static final String SSRC_ELEMENT_NAME = "ssrc";
        private MediaDirection direction;
        private String endpoint;
        private int expire = -1;
        @Deprecated
        private String host;
        private String id;
        private Boolean initiator;
        private Integer lastN;
        private final List<PayloadTypePacketExtension> payloadTypes = new ArrayList();
        @Deprecated
        private int rtcpPort;
        private RTPLevelRelayType rtpLevelRelayType;
        @Deprecated
        private int rtpPort;
        private final List<SourcePacketExtension> sources = new LinkedList();
        private int[] ssrcs = ColibriConferenceIQ.NO_SSRCS;
        private IceUdpTransportPacketExtension transport;

        public boolean addPayloadType(PayloadTypePacketExtension payloadType) {
            if (payloadType == null) {
                throw new NullPointerException("payloadType");
            }
            payloadType.setNamespace(null);
            for (ParameterPacketExtension p : payloadType.getParameters()) {
                p.setNamespace(null);
            }
            return this.payloadTypes.contains(payloadType) ? false : this.payloadTypes.add(payloadType);
        }

        public synchronized boolean addSource(SourcePacketExtension source) {
            if (source == null) {
                throw new NullPointerException(SourcePacketExtension.ELEMENT_NAME);
            }
            return this.sources.contains(source) ? false : this.sources.add(source);
        }

        public synchronized boolean addSSRC(int ssrc) {
            boolean z = false;
            synchronized (this) {
                for (int i : this.ssrcs) {
                    if (i == ssrc) {
                        break;
                    }
                }
                int[] newSSRCs = new int[(this.ssrcs.length + 1)];
                System.arraycopy(this.ssrcs, 0, newSSRCs, 0, this.ssrcs.length);
                newSSRCs[this.ssrcs.length] = ssrc;
                this.ssrcs = newSSRCs;
                z = true;
            }
            return z;
        }

        public MediaDirection getDirection() {
            return this.direction == null ? MediaDirection.SENDRECV : this.direction;
        }

        public String getEndpoint() {
            return this.endpoint;
        }

        public int getExpire() {
            return this.expire;
        }

        @Deprecated
        public String getHost() {
            return this.host;
        }

        public String getID() {
            return this.id;
        }

        public Integer getLastN() {
            return this.lastN;
        }

        public List<PayloadTypePacketExtension> getPayloadTypes() {
            return Collections.unmodifiableList(this.payloadTypes);
        }

        @Deprecated
        public int getRTCPPort() {
            return this.rtcpPort;
        }

        public RTPLevelRelayType getRTPLevelRelayType() {
            return this.rtpLevelRelayType;
        }

        @Deprecated
        public int getRTPPort() {
            return this.rtpPort;
        }

        public synchronized List<SourcePacketExtension> getSources() {
            return new ArrayList(this.sources);
        }

        public synchronized int[] getSSRCs() {
            return this.ssrcs.length == 0 ? ColibriConferenceIQ.NO_SSRCS : (int[]) this.ssrcs.clone();
        }

        public IceUdpTransportPacketExtension getTransport() {
            return this.transport;
        }

        public Boolean isInitiator() {
            return this.initiator;
        }

        public boolean removePayloadType(PayloadTypePacketExtension payloadType) {
            return this.payloadTypes.remove(payloadType);
        }

        public synchronized boolean removeSource(SourcePacketExtension source) {
            return this.sources.remove(source);
        }

        public synchronized boolean removeSSRC(int ssrc) {
            boolean z = true;
            synchronized (this) {
                if (this.ssrcs.length != 1) {
                    int i = 0;
                    while (i < this.ssrcs.length) {
                        if (this.ssrcs[i] == ssrc) {
                            int[] newSSRCs = new int[(this.ssrcs.length - 1)];
                            if (i != 0) {
                                System.arraycopy(this.ssrcs, 0, newSSRCs, 0, i);
                            }
                            if (i != newSSRCs.length) {
                                System.arraycopy(this.ssrcs, i + 1, newSSRCs, i, newSSRCs.length - i);
                            }
                            this.ssrcs = newSSRCs;
                        } else {
                            i++;
                        }
                    }
                    z = false;
                } else if (this.ssrcs[0] == ssrc) {
                    this.ssrcs = ColibriConferenceIQ.NO_SSRCS;
                } else {
                    z = false;
                }
            }
            return z;
        }

        public void setDirection(MediaDirection direction) {
            this.direction = direction;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public void setExpire(int expire) {
            if (expire == -1 || expire >= 0) {
                this.expire = expire;
                return;
            }
            throw new IllegalArgumentException(EXPIRE_ATTR_NAME);
        }

        @Deprecated
        public void setHost(String host) {
            this.host = host;
        }

        public void setID(String id) {
            this.id = id;
        }

        public void setInitiator(Boolean initiator) {
            this.initiator = initiator;
        }

        public void setLastN(Integer lastN) {
            this.lastN = lastN;
        }

        @Deprecated
        public void setRTCPPort(int rtcpPort) {
            this.rtcpPort = rtcpPort;
        }

        public void setRTPLevelRelayType(RTPLevelRelayType rtpLevelRelayType) {
            this.rtpLevelRelayType = rtpLevelRelayType;
        }

        public void setRTPLevelRelayType(String s) {
            setRTPLevelRelayType(RTPLevelRelayType.parseRTPLevelRelayType(s));
        }

        @Deprecated
        public void setRTPPort(int rtpPort) {
            this.rtpPort = rtpPort;
        }

        public void setSSRCs(int[] ssrcs) {
            int[] iArr = (ssrcs == null || ssrcs.length == 0) ? ColibriConferenceIQ.NO_SSRCS : (int[]) ssrcs.clone();
            this.ssrcs = iArr;
        }

        public void setTransport(IceUdpTransportPacketExtension transport) {
            this.transport = transport;
        }

        public void toXML(StringBuilder xml) {
            xml.append('<').append("channel");
            MediaDirection direction = getDirection();
            if (!(direction == null || direction == MediaDirection.SENDRECV)) {
                xml.append(' ').append(DIRECTION_ATTR_NAME).append("='").append(direction.toString()).append('\'');
            }
            String endpoint = getEndpoint();
            if (endpoint != null) {
                xml.append(' ').append("endpoint").append("='").append(endpoint).append('\'');
            }
            int expire = getExpire();
            if (expire >= 0) {
                xml.append(' ').append(EXPIRE_ATTR_NAME).append("='").append(expire).append('\'');
            }
            String host = getHost();
            if (host != null) {
                xml.append(' ').append("host").append("='").append(host).append('\'');
            }
            String id = getID();
            if (id != null) {
                xml.append(' ').append("id").append("='").append(id).append('\'');
            }
            Boolean initiator = isInitiator();
            if (initiator != null) {
                xml.append(' ').append("initiator").append("='").append(initiator).append('\'');
            }
            Integer lastN = getLastN();
            if (lastN != null) {
                xml.append(' ').append(LAST_N_ATTR_NAME).append("='").append(lastN).append('\'');
            }
            int rtcpPort = getRTCPPort();
            if (rtcpPort > 0) {
                xml.append(' ').append(RTCP_PORT_ATTR_NAME).append("='").append(rtcpPort).append('\'');
            }
            RTPLevelRelayType rtpLevelRelayType = getRTPLevelRelayType();
            if (rtpLevelRelayType != null) {
                xml.append(' ').append(RTP_LEVEL_RELAY_TYPE_ATTR_NAME).append("='").append(rtpLevelRelayType).append('\'');
            }
            int rtpPort = getRTPPort();
            if (rtpPort > 0) {
                xml.append(' ').append(RTP_PORT_ATTR_NAME).append("='").append(rtpPort).append('\'');
            }
            List<PayloadTypePacketExtension> payloadTypes = getPayloadTypes();
            boolean hasPayloadTypes = !payloadTypes.isEmpty();
            List<SourcePacketExtension> sources = getSources();
            boolean hasSources = !sources.isEmpty();
            int[] ssrcs = getSSRCs();
            boolean hasSSRCs = ssrcs.length != 0;
            IceUdpTransportPacketExtension transport = getTransport();
            boolean hasTransport = transport != null;
            if (hasPayloadTypes || hasSources || hasSSRCs || hasTransport) {
                xml.append('>');
                if (hasPayloadTypes) {
                    for (PayloadTypePacketExtension payloadType : payloadTypes) {
                        xml.append(payloadType.toXML());
                    }
                }
                if (hasSources) {
                    for (SourcePacketExtension source : sources) {
                        xml.append(source.toXML());
                    }
                }
                if (hasSSRCs) {
                    for (int i : ssrcs) {
                        xml.append('<').append("ssrc").append('>').append(Long.toString(((long) i) & 4294967295L)).append("</").append("ssrc").append('>');
                    }
                }
                if (hasTransport) {
                    xml.append(transport.toXML());
                }
                xml.append("</").append("channel").append('>');
                return;
            }
            xml.append(" />");
        }
    }

    public static class Content {
        public static final String ELEMENT_NAME = "content";
        public static final String NAME_ATTR_NAME = "name";
        private final List<Channel> channels = new LinkedList();
        private String name;

        public Content(String name) {
            setName(name);
        }

        public boolean addChannel(Channel channel) {
            if (channel != null) {
                return this.channels.contains(channel) ? false : this.channels.add(channel);
            } else {
                throw new NullPointerException("channel");
            }
        }

        public Channel getChannel(int channelIndex) {
            return (Channel) getChannels().get(channelIndex);
        }

        public Channel getChannel(String channelID) {
            for (Channel channel : getChannels()) {
                if (channelID.equals(channel.getID())) {
                    return channel;
                }
            }
            return null;
        }

        public int getChannelCount() {
            return getChannels().size();
        }

        public List<Channel> getChannels() {
            return Collections.unmodifiableList(this.channels);
        }

        public String getName() {
            return this.name;
        }

        public boolean removeChannel(Channel channel) {
            return this.channels.remove(channel);
        }

        public void setName(String name) {
            if (name == null) {
                throw new NullPointerException("name");
            }
            this.name = name;
        }

        public void toXML(StringBuilder xml) {
            xml.append('<').append("content");
            xml.append(' ').append("name").append("='").append(getName()).append('\'');
            List<Channel> channels = getChannels();
            if (channels.size() == 0) {
                xml.append(" />");
                return;
            }
            xml.append('>');
            for (Channel channel : channels) {
                channel.toXML(xml);
            }
            xml.append("</").append("content").append('>');
        }
    }

    public boolean addContent(String contentName) {
        return addContent(new Content(contentName));
    }

    public boolean addContent(Content content) {
        if (content != null) {
            return this.contents.contains(content) ? false : this.contents.add(content);
        } else {
            throw new NullPointerException("content");
        }
    }

    public String getChildElementXML() {
        StringBuilder xml = new StringBuilder();
        xml.append('<').append("conference");
        xml.append(" xmlns='").append(NAMESPACE).append('\'');
        String id = getID();
        if (id != null) {
            xml.append(' ').append("id").append("='").append(id).append('\'');
        }
        List<Content> contents = getContents();
        if (contents.size() == 0) {
            xml.append(" />");
        } else {
            xml.append('>');
            for (Content content : contents) {
                content.toXML(xml);
            }
            xml.append("</").append("conference").append('>');
        }
        return xml.toString();
    }

    public Content getContent(String contentName) {
        for (Content content : getContents()) {
            if (contentName.equals(content.getName())) {
                return content;
            }
        }
        return null;
    }

    public List<Content> getContents() {
        return Collections.unmodifiableList(this.contents);
    }

    public String getID() {
        return this.id;
    }

    public Content getOrCreateContent(String contentName) {
        Content content = getContent(contentName);
        if (content != null) {
            return content;
        }
        content = new Content(contentName);
        addContent(content);
        return content;
    }

    public boolean removeContent(Content content) {
        return this.contents.remove(content);
    }

    public void setID(String id) {
        this.id = id;
    }
}
